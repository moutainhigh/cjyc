package com.cjyc.common.system.service.impl;

import com.Pingxx.model.OrderModel;
import com.cjyc.common.model.dao.IOrderCarDao;
import com.cjyc.common.model.dao.IOrderDao;
import com.cjyc.common.model.dto.customer.pingxx.SweepCodeDto;
import com.cjyc.common.model.dto.customer.pingxx.ValidateSweepCodeDto;
import com.cjyc.common.model.entity.OrderCar;
import com.cjyc.common.model.enums.ClientEnum;
import com.cjyc.common.model.enums.PayModeEnum;
import com.cjyc.common.model.enums.PayStateEnum;
import com.cjyc.common.model.enums.ResultEnum;
import com.cjyc.common.model.enums.customer.CustomerTypeEnum;
import com.cjyc.common.model.enums.order.OrderCarStateEnum;
import com.cjyc.common.model.exception.CommonException;
import com.cjyc.common.model.keys.RedisKeys;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.customer.order.ValidateReceiptCarPayVo;
import com.cjyc.common.model.vo.customer.order.ValidateSweepCodePayVo;
import com.cjyc.common.system.config.PingProperty;
import com.cjyc.common.system.service.ICsPingPayService;
import com.cjyc.common.system.service.ICsTransactionService;
import com.cjyc.common.system.util.RedisLock;
import com.cjyc.common.system.util.RedisUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pingplusplus.Pingpp;
import com.pingplusplus.exception.*;
import com.pingplusplus.model.Charge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Author: Hut
 * @Date: 2019/12/9 19:29
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
public class CsPingPayServiceImpl implements ICsPingPayService {

    @Autowired
    private ICsTransactionService cStransactionService;

    @Resource
    private IOrderDao orderDao;

    @Resource
    private IOrderCarDao orderCarDao;

    @Resource
    private RedisLock redisLock;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public Charge sweepDriveCode(SweepCodeDto sweepCodeDto) throws RateLimitException, APIException, ChannelException, InvalidRequestException,
            APIConnectionException, AuthenticationException, FileNotFoundException {
        ValidateSweepCodeDto validateSweepCodeDto = new ValidateSweepCodeDto();
        validateSweepCodeDto.setTaskId(sweepCodeDto.getTaskId());
        ResultVo<ValidateSweepCodePayVo> resultVo = validateCarPayState(validateSweepCodeDto,true);

        if(ResultEnum.SUCCESS.getCode() != resultVo.getCode()){
            throw new CommonException(resultVo.getMsg());
        }
        BigDecimal amount = resultVo.getData().getAmount();
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new CommonException(("无需支付"));
        }
        OrderModel om = new OrderModel();

        om.setClientIp(sweepCodeDto.getIp());
        om.setPingAppId(PingProperty.driverAppId);
        //创建Charge对象
        Charge charge = new Charge();
        try {
            List<String> orderCarNosList = cStransactionService.getOrderCarNosByTaskId(sweepCodeDto.getTaskId());
            BigDecimal freightFee = cStransactionService.getAmountByOrderCarNos(orderCarNosList);
            om.setAmount(freightFee);
            om.setDriver_code("");
            om.setOrderCarIds(om.getOrderCarIds());
            om.setChannel(sweepCodeDto.getChannel());
            om.setSubject("司机端收款码功能!");
            om.setBody("生成二维码！");
            om.setChargeType("2");
            om.setClientType(String.valueOf(ClientEnum.APP_DRIVER.code));
            om.setDescription("韵车订单号："+om.getOrderNo());
            charge = createDriverCode(om);

            cStransactionService.saveTransactions(charge, "0");
        } catch (Exception e) {
            log.error("扫码支付异常",e);
        }
        return charge;
    }

    private Charge createDriverCode(OrderModel om) throws RateLimitException, APIException, ChannelException,InvalidRequestException,
            APIConnectionException, AuthenticationException,FileNotFoundException{
        initPingApiKey();
        Map<String, Object> params = new HashMap<String, Object>();
        Calendar calendar = Calendar.getInstance();
        params.put("order_no", Integer.toString(calendar.get(Calendar.YEAR)) + System.currentTimeMillis()); // 商户订单号, 必传
        Map<String, String> app = new HashMap<String, String>();
        app.put("id", om.getPingAppId());
        params.put("app", app);
        params.put("channel", om.getChannel());// alipay_qr 支付宝扫码支付 /wx_pub_qr 微信扫码支付
        params.put("amount", om.getAmount()); // 订单总金额，单位：分, 必传
        params.put("client_ip", om.getClientIp()); // 客户端的 IP 地址 (IPv4 格式，要求商户上传真实的，渠道可能会判断), 必传
        params.put("currency", "cny"); // 仅支持人民币 cny, 必传
        params.put("subject", om.getSubject()); // 商品的标题, 必传
        params.put("body", om.getBody()); // 商品的描述信息, 必传
        params.put("description", om.getDescription()); // 备注：订单号
        Map<String, Object> meta = new HashMap<String,Object>();
        meta.put("chargeType", om.getChargeType());//0:定金	1：尾款
        //自定义存储字段
        meta.put("orderNo", om.getOrderNo());	//订单号
        meta.put("orderCarIds",om.getOrderCarIds());//订单Id
        meta.put("driver_code", om.getDriver_code());//司机Code
        meta.put("order_type", om.getOrder_type());
        meta.put("driver_name", om.getDriver_name());
        meta.put("driver_phone", om.getDriver_phone());
        meta.put("back_type", om.getBack_type());
        //当为微信支付是需要product_id
        if("wx_pub_qr".equals(om.getChannel())){
            Map<String, Object> extra  = new HashMap<String,Object>();
            extra.put("product_id", Integer.toString(calendar.get(Calendar.YEAR)) + System.currentTimeMillis());
            params.put("extra", extra);
        }
        params.put("metadata",meta);//自定义参数
        Charge charge = Charge.create(params); // 创建 Charge 对象 方法
        return charge;
    }

    private void initPingApiKey() throws FileNotFoundException {
        Pingpp.apiKey = PingProperty.apiKey;
        //Pingpp.overrideApiBase("https://sapi.pingxx.com");//此接口为ping++协助测试的接口 升级完成后注释掉 20181023 add
        System.setProperty("https.protocols", "TLSv1.2");//20181023 添加 (TLSv1.2升级配置)
        Pingpp.privateKeyPath = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX+"your_rsa_private_key_pkcs.pem").getPath();
    }

    @Override
    public ResultVo<ValidateSweepCodePayVo> validateCarPayState(ValidateSweepCodeDto validateSweepCodeDto, boolean addLock) {
        Long taskId = validateSweepCodeDto.getTaskId();

        List<String> orderCarNosList = cStransactionService.getOrderCarNosByTaskId(taskId);
        List<com.cjyc.common.model.entity.Order> list = orderDao.findListByCarNos(orderCarNosList);
        if (CollectionUtils.isEmpty(list)) {
            return BaseResultUtil.fail("订单信息丢失");
        }
        if (list.size() > 1) {
            return BaseResultUtil.fail("仅支持同一订单内的车辆签收");
        }

        com.cjyc.common.model.entity.Order order = list.get(0);

        int isNeedPay = 0; //0不需要支付，1支付
        BigDecimal amount = BigDecimal.ZERO;
        List<String> orderCarNos = Lists.newArrayList();
        Set<String> lockKeySet = Sets.newHashSet();
        try {
            List<OrderCar> carList = orderCarDao.findListByNos(orderCarNosList);
            if(CollectionUtils.isEmpty(carList)){
                return BaseResultUtil.fail("至少包含一辆车");
            }
            for (OrderCar orderCar : carList) {
                if (orderCar == null || orderCar.getNo() == null) {
                    return BaseResultUtil.fail("订单车辆信息丢失");
                }
                if (orderCar.getState() >= OrderCarStateEnum.SIGNED.code) {
                    return BaseResultUtil.fail("订单车辆{0}已签收过，请刷新后重试", orderCar.getNo());
                }

                if(addLock){
                    String lockKey = RedisKeys.getWlCollectPayLockKey(orderCar.getNo());
                    String value = redisUtils.get(lockKey);
                    if (value != null && !value.equals(validateSweepCodeDto.getLoginId().toString())) {
                        return BaseResultUtil.fail("订单车辆{0}正在支付中", orderCar.getNo());
                    }
                    if (value != null) {
                        redisUtils.delete(lockKey);
                    }
                    if (!redisLock.lock(lockKey, validateSweepCodeDto.getLoginId(), 1800000, 100, 300)) {
                        return BaseResultUtil.fail("锁定车辆失败");
                    }
                    lockKeySet.add(lockKey);
                }

                //是否需要支付
                if (PayModeEnum.PERIOD.code == order.getPayType()) {
                    //账期
                    isNeedPay = 0;
                } else if (PayModeEnum.PREPAY.code == order.getPayType()) {
                    //预付
                    if (PayStateEnum.PAID.code != orderCar.getWlPayState()) {
                        return BaseResultUtil.fail("支付车辆{0}支付状态异常，预付未支付", orderCar.getNo());
                    }
                    isNeedPay = 0;
                } else {
                    //时付
                    if (PayStateEnum.PAID.code == orderCar.getWlPayState()) {
                        return BaseResultUtil.fail("订单车辆{0}已支付过，请刷新后重试", orderCar.getNo());
                    }
                    isNeedPay = 1;
                    if(CustomerTypeEnum.COOPERATOR.code == order.getCustomerType()){
                        amount = amount.add(orderCar.getTotalFee());
                    }else{
                        amount = amount.add(orderCar.getTotalFee()).subtract(orderCar.getCouponOffsetFee());
                    }
                }
                orderCarNos.add(orderCar.getNo());
            }
            if(CollectionUtils.isEmpty(orderCarNos)){
                return BaseResultUtil.fail("至少包含一辆车编号");
            }
            if(amount.compareTo(BigDecimal.ZERO) <= 0){
                isNeedPay = 0;
            }

            ValidateSweepCodePayVo resVo = new ValidateSweepCodePayVo();
            resVo.setAmount(amount);
            resVo.setIsNeedPay(isNeedPay);
            resVo.setTaskId(taskId);
            return BaseResultUtil.success(resVo);
        } finally {
            if(addLock && isNeedPay == 0){
                //解锁
                redisLock.releaseLock(lockKeySet);
            }
        }

    }
}
