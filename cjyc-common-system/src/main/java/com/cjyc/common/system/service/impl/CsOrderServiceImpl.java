package com.cjyc.common.system.service.impl;

import com.cjyc.common.model.dao.IOrderCarDao;
import com.cjyc.common.model.dao.IOrderDao;
import com.cjyc.common.model.dto.web.order.*;
import com.cjyc.common.model.entity.*;
import com.cjyc.common.model.enums.PayModeEnum;
import com.cjyc.common.model.enums.ResultEnum;
import com.cjyc.common.model.enums.SendNoTypeEnum;
import com.cjyc.common.model.enums.city.CityLevelEnum;
import com.cjyc.common.model.enums.customer.CustomerTypeEnum;
import com.cjyc.common.model.enums.order.OrderCarStateEnum;
import com.cjyc.common.model.enums.order.OrderChangeTypeEnum;
import com.cjyc.common.model.enums.order.OrderStateEnum;
import com.cjyc.common.model.exception.ParameterException;
import com.cjyc.common.model.exception.ServerException;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.entity.defined.FullCity;
import com.cjyc.common.system.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单公共业务
 * @author JPG
 */
@Service
@Transactional(rollbackFor = RuntimeException.class)
public class CsOrderServiceImpl implements ICsOrderService {

    @Resource
    private IOrderDao orderDao;
    @Resource
    private IOrderCarDao orderCarDao;
    @Resource
    private ICsCityService csCityService;
    @Resource
    private ICsSendNoService csSendNoService;
    @Resource
    private ICsCustomerService csCustomerService;
    @Resource
    private ICsStoreService csStoreService;
    @Resource
    private ICsOrderChangeLogService orderChangeLogService;
    @Resource
    private ICsCustomerContactService csCustomerContactService;


    @Override
    public ResultVo save(SaveOrderDto paramsDto, OrderStateEnum stateEnum) {
        //获取参数
        Long orderId = paramsDto.getOrderId();

        Order order = null;
        boolean newOrderFlag = false;
        if (orderId != null) {
            //更新订单
            order = orderDao.selectById(orderId);
        }
        if (order == null) {
            //新建订单
            newOrderFlag = true;
            order = new Order();

        }
        BeanUtils.copyProperties(paramsDto, order);
        //查询三级城市
        FullCity startFullCity = csCityService.findFullCity(paramsDto.getStartAreaCode(), CityLevelEnum.PROVINCE);
        copyOrderStartCity(startFullCity, order);
        FullCity endFullCity = csCityService.findFullCity(paramsDto.getEndAreaCode(), CityLevelEnum.PROVINCE);
        copyOrderEndCity(endFullCity, order);

        /**1、组装订单数据
         */
        if (newOrderFlag) {
            order.setNo(csSendNoService.getNo(SendNoTypeEnum.ORDER));
        }
        order.setState(stateEnum.code);
        order.setSource(order.getSource() == null ? paramsDto.getClientId() : order.getSource());
        order.setCreateTime(System.currentTimeMillis());

        //更新或插入订单
        int row = newOrderFlag ? orderDao.insert(order) : orderDao.updateById(order);

        /**2、更新或保存车辆信息*/
        List<SaveOrderCarDto> carDtoList = paramsDto.getOrderCarList();
        if (carDtoList == null || carDtoList.isEmpty()) {
            //没有车辆，结束
            return BaseResultUtil.success();
        }

        //费用统计变量
        //删除旧的车辆数据
        if (!newOrderFlag) {
            orderCarDao.deleteBatchByOrderId(order.getId());
        }
        int noCount = 1;
        for (SaveOrderCarDto dto : carDtoList) {
            if (dto == null) {
                continue;
            }

            OrderCar orderCar = new OrderCar();
            //复制数据
            BeanUtils.copyProperties(dto, orderCar);
            //填充数据
            orderCar.setOrderNo(order.getNo());
            orderCar.setOrderId(order.getId());
            orderCar.setNo(order.getNo() + "-" + noCount);
            orderCar.setState(OrderCarStateEnum.WAIT_ROUTE.code);
            orderCarDao.insert(orderCar);
            //统计数量
            noCount++;
        }
        return BaseResultUtil.success();
    }

    @Override
    public ResultVo commit(CommitOrderDto paramsDto) {
        //获取参数
        Long orderId = paramsDto.getOrderId();

        Order order = null;
        boolean newOrderFlag = false;
        if (orderId != null) {
            //更新订单
            order = orderDao.selectById(orderId);
        }
        if (order == null) {
            //新建订单
            newOrderFlag = true;
            order = new Order();
        }
        BeanUtils.copyProperties(paramsDto, order);
        //查询三级城市
        FullCity startFullCity = csCityService.findFullCity(paramsDto.getStartAreaCode(), CityLevelEnum.PROVINCE);
        copyOrderStartCity(startFullCity, order);
        FullCity endFullCity = csCityService.findFullCity(paramsDto.getEndAreaCode(), CityLevelEnum.PROVINCE);
        copyOrderEndCity(endFullCity, order);

        //验证用户
        ResultVo<Customer> validateCustomerResult = validateCustomer(paramsDto);
        if(ResultEnum.SUCCESS.getCode() != validateCustomerResult.getCode()){
            return validateCustomerResult;
        }
        Customer customer= validateCustomerResult.getData();
        //订单中添加客户ID
        order.setCustomerId(customer.getId());
        /**1、组装订单数据
         *
         */
        if (newOrderFlag) {
            order.setNo(csSendNoService.getNo(SendNoTypeEnum.ORDER));
        }
        //计算所属业务中心ID
        if(paramsDto.getStartStoreId() > 0){
            order.setStartBelongStoreId(paramsDto.getStartStoreId());
        }else{
            //查询地址所属业务中心
            Store startBelongStore = csStoreService.findOneBelongByAreaCode(order.getStartAreaCode());
            if(startBelongStore != null){
                order.setStartBelongStoreId(startBelongStore.getId());
            }
        }
        if(paramsDto.getEndStoreId() > 0){
            order.setStartBelongStoreId(paramsDto.getEndStoreId());
        }else{
            //查询地址所属业务中心
            Store endBelongStore = csStoreService.findOneBelongByAreaCode(order.getEndAreaCode());
            if(endBelongStore != null){
                order.setStartBelongStoreId(endBelongStore.getId());
            }
        }


        order.setState(OrderStateEnum.SUBMITTED.code);
        order.setSource(order.getSource() == null ? paramsDto.getClientId() : order.getSource());
        order.setCreateTime(System.currentTimeMillis());

        //更新或插入订单
        int row = newOrderFlag ? orderDao.insert(order) : orderDao.updateById(order);
        if (row <= 0) {
            return BaseResultUtil.fail("订单未修改，提交失败");
        }

        /**2、更新或保存车辆信息*/
        List<CommitOrderCarDto> carDtoList = paramsDto.getOrderCarList();
        if (carDtoList == null || carDtoList.isEmpty()) {
            throw new ParameterException("订单车辆不能为空");
        }
        //删除旧的车辆数据
        if (!newOrderFlag) {
            orderCarDao.deleteBatchByOrderId(order.getId());
        }
        //费用统计变量
        int noCount = 0;
        for (CommitOrderCarDto dto : carDtoList) {
            if (dto == null) {
                continue;
            }
            //统计数量
            noCount++;
            OrderCar orderCar = new OrderCar();
            //复制数据
            BeanUtils.copyProperties(dto, orderCar);
            //填充数据
            orderCar.setOrderNo(order.getNo());
            orderCar.setOrderId(order.getId());
            orderCar.setNo(order.getNo() + "-" + noCount);
            orderCar.setState(OrderCarStateEnum.WAIT_ROUTE.code);
            orderCar.setPickFee(dto.getPickFee() == null ? BigDecimal.ZERO : dto.getPickFee());
            orderCar.setTrunkFee(dto.getTrunkFee() == null ? BigDecimal.ZERO : dto.getTrunkFee());
            orderCar.setBackFee(dto.getBackFee() == null ? BigDecimal.ZERO : dto.getBackFee());
            orderCar.setAddInsuranceFee(dto.getAddInsuranceFee() == null ? BigDecimal.ZERO : dto.getAddInsuranceFee());
            orderCarDao.insert(orderCar);
        }
        order.setCarNum(noCount);
        if (noCount == 0) {
            throw new ParameterException("订单至少包含一辆车");
        }
        orderDao.updateById(order);

        //记录发车人和收车人
        csCustomerContactService.saveByOrder(order);
        return BaseResultUtil.success(order.getNo());
    }

    private ResultVo<Customer> validateCustomer(CommitOrderDto paramsDto) {
        Customer customer = null;
        if (paramsDto.getCustomerId() != null) {
            customer = csCustomerService.getByUserId(paramsDto.getCustomerId(),true);
            if(customer != null && customer.getName().equals(paramsDto.getCustomerName())){
                return BaseResultUtil.fail(ResultEnum.CREATE_NEW_CUSTOMER.getCode(),
                        "客户手机号存在，名称不一致：新名称（{0}）旧名称（{1}），请返回订单重新选择客户",
                        paramsDto.getCustomerName(),customer.getName());
            }
        }
        if(customer == null){
            customer = csCustomerService.getByPhone(paramsDto.getCustomerPhone(),true);
            if(customer != null && customer.getName().equals(paramsDto.getCustomerName())){
                return BaseResultUtil.fail(ResultEnum.CREATE_NEW_CUSTOMER.getCode(),
                        "客户手机号存在，名称不一致：新名称（{0}）旧名称（{1}），请返回订单重新选择客户",
                        paramsDto.getCustomerName(),customer.getName());
            }
        }
        if (customer == null) {
            customer = new Customer();
            if (paramsDto.getCustomerType() == CustomerTypeEnum.INDIVIDUAL.code) {
                if (paramsDto.getCreateCustomerFlag()) {
                    customer.setName(paramsDto.getCustomerName());
                    customer.setContactMan(paramsDto.getCustomerName());
                    customer.setContactPhone(paramsDto.getCustomerPhone());
                    customer.setType(CustomerTypeEnum.INDIVIDUAL.code);
                    //customer.setInitial()
                    customer.setState(1);
                    customer.setPayMode(PayModeEnum.COLLECT.code);
                    customer.setCreateTime(System.currentTimeMillis());
                    customer.setCreateUserId(paramsDto.getUserId());
                    //添加
                    csCustomerService.save(customer);
                } else {
                    return BaseResultUtil.getVo(ResultEnum.CREATE_NEW_CUSTOMER.getCode(), ResultEnum.CREATE_NEW_CUSTOMER.getMsg());
                }
            } else {
                return BaseResultUtil.fail("企业客户/合伙人不存在");
            }
        }
        return BaseResultUtil.success(customer);
    }

    /**
     * 审核订单
     *
     * @param reqDto
     * @author JPG
     * @since 2019/11/5 15:03
     */
    @Override
    public ResultVo check(CheckOrderDto reqDto) {
        Order order = orderDao.selectById(reqDto.getOrderId());
        //验证必要信息是否完全
        validateOrderFeild(order);

        List<OrderCar> orderCarList = orderCarDao.findByOrderId(order.getId());
        if (orderCarList == null || orderCarList.isEmpty()) {
            return BaseResultUtil.fail("[订单车辆]-为空");
        }

        //验证物流券费用
        /*BigDecimal wlTotalFee = orderCarDao.getWLTotalFee(reqDto.getOrderId());
        BigDecimal couponAmount = BigDecimal.ZERO;
        if (order.getCouponSendId() != null) {
            couponAmount = couponSendService.getAmountById(order.getCouponSendId(), wlTotalFee);
        }
        order.setCouponOffsetFee(couponAmount);*/

        //均摊优惠券费用
        BigDecimal totalCouponOffsetFee = order.getCouponOffsetFee() == null ? BigDecimal.ZERO : order.getCouponOffsetFee();
        if (totalCouponOffsetFee.compareTo(BigDecimal.ZERO) > 0) {
            shareCouponOffsetFee(order, orderCarList);
        }

        //均摊总费用
        BigDecimal totalFee = order.getTotalFee() == null ? BigDecimal.ZERO : order.getTotalFee();
        if (totalFee.compareTo(BigDecimal.ZERO) > 0) {
            shareTotalFee(order, orderCarList);
        }
        for (OrderCar orderCar : orderCarList) {
            orderCarDao.updateById(orderCar);
        }

        //根据到付和预付置不同状态
        if(order.getPayType() != PayModeEnum.PREPAID.code){
            order.setState(OrderStateEnum.WAIT_PREPAY.code);
            //TODO 支付通知
        }else{
            order.setState(OrderStateEnum.CHECKED.code);
        }
        orderDao.updateById(order);

        //TODO 处理优惠券为使用状态，优惠券有且仅能验证一次，修改时怎么保证
        //TODO 路由轨迹

        return BaseResultUtil.success();
    }

    /**
     * 验证订单必要信息
     * @author JPG
     * @since 2019/11/6 19:45
     * @param order
     */
    private void validateOrderFeild(Order order) {
        if (order == null) {
            throw new ParameterException("[订单]-不存在");
        }
        if (order.getState() <= OrderStateEnum.WAIT_SUBMIT.code) {
            throw new ParameterException("[订单]-未提交，无法审核");
        }
        if (order.getState() >= OrderStateEnum.CHECKED.code) {
            throw new ParameterException("[订单]-已经审核过，无法审核");
        }
        if (order.getId() == null || order.getNo() == null) {
            throw new ParameterException("[订单]-订单编号不能为空");
        }
        if (order.getCustomerId() == null) {
            throw new ParameterException("[订单]-客户不存在");
        }
        if (order.getStartProvinceCode() == null
                || order.getStartCityCode() == null
                || order.getStartAreaCode() == null
                || order.getStartAddress() == null
                || order.getEndProvinceCode() == null
                || order.getEndCityCode() == null
                || order.getEndAreaCode() == null
                || order.getEndAddress() == null) {
            throw new ParameterException("[订单]-地址不完整");
        }
        if (order.getCarNum() == null || order.getCarNum() <= 0) {
            throw new ParameterException("[订单]-车辆数不能小于一辆");
        }
        if (order.getPickType() == null
                || order.getPickContactPhone() == null) {
            throw new ParameterException("[订单]-提车联系人不能为空");
        }
        if (order.getBackType() == null
                || order.getBackContactPhone() == null) {
            throw new ParameterException("收车联系人不能为空");
        }

    }

    /**
     * 分配订单
     *
     * @param paramsDto
     * @author JPG
     * @since 2019/11/5 16:05
     */
    @Override
    public ResultVo allot(AllotOrderDto paramsDto) {
        Order order = orderDao.selectById(paramsDto.getOrderId());
        if (order == null || order.getState() >= OrderStateEnum.WAIT_RECHECK.code) {
            return BaseResultUtil.fail("订单不允许修改");
        }
        order.setAllotToUserId(paramsDto.getToUserId());
        order.setAllotToUserName(paramsDto.getToUserName());
        orderDao.updateById(order);
        return BaseResultUtil.success();
    }

    /**
     * 驳回订单
     *
     * @param paramsDto
     * @author JPG
     * @since 2019/11/5 16:07
     */
    @Override
    public ResultVo reject(RejectOrderDto paramsDto) {
        Order order = orderDao.selectById(paramsDto.getOrderId());
        if (order == null) {
            return BaseResultUtil.fail("[订单]-不存在");
        }
        Integer oldState = order.getState();
        if (oldState <= OrderStateEnum.WAIT_SUBMIT.code) {
            return BaseResultUtil.fail("[订单]-未提交，无法驳回");
        }
        if (oldState > OrderStateEnum.CHECKED.code) {
            return BaseResultUtil.fail("[订单]-已经运输无法驳回");
        }
        orderDao.updateStateById(OrderStateEnum.WAIT_SUBMIT.code, order.getId());
        //添加操作日志
        orderChangeLogService.save(order, OrderChangeTypeEnum.REJECT,
                new Object[]{oldState, OrderStateEnum.WAIT_SUBMIT.code, paramsDto.getReason()},
                new Object[]{paramsDto.getUserId(), paramsDto.getUserName()});
        //TODO 发送消息给创建人
        return BaseResultUtil.success();
    }

    /**
     * 取消订单
     * @author JPG
     * @since 2019/11/5 16:32
     * @param paramsDto
     */
    @Override
    public ResultVo cancel(CancelOrderDto paramsDto) {
        //取消订单
        Order order = orderDao.selectById(paramsDto.getOrderId());
        if (order == null) {
            return BaseResultUtil.fail("订单不存在");
        }
        if (order.getState() >= OrderStateEnum.CHECKED.code) {
            return BaseResultUtil.fail("当前订单状态不允许取消");
        }
        Integer oldState = order.getState();
        order.setState(OrderStateEnum.F_CANCEL.code);
        orderDao.updateById(order);

        //添加操作日志
        orderChangeLogService.save(order, OrderChangeTypeEnum.CANCEL,
                new Object[]{oldState, OrderStateEnum.F_CANCEL.code, paramsDto.getReason()},
                new Object[]{paramsDto.getUserId(), paramsDto.getUserName()});
        //TODO 发送消息
        return BaseResultUtil.success();
    }

    @Override
    public ResultVo obsolete(CancelOrderDto paramsDto) {
        //作废订单
        Order order = orderDao.selectById(paramsDto.getOrderId());
        if (order == null) {
            return BaseResultUtil.fail("订单不存在");
        }
        if (order.getState() > OrderStateEnum.CHECKED.code) {
            return BaseResultUtil.fail("当前订单状态不允许作废");
        }
        Integer oldState = order.getState();
        order.setState(OrderStateEnum.F_OBSOLETE.code);
        orderDao.updateById(order);

        //添加操作日志
        orderChangeLogService.save(order, OrderChangeTypeEnum.OBSOLETE,
                new Object[]{oldState, OrderStateEnum.F_OBSOLETE.code, paramsDto.getReason()},
                new Object[]{paramsDto.getUserId(), paramsDto.getUserName()});

        return BaseResultUtil.success();
    }

    @Override
    public ResultVo changePrice(ChangePriceOrderDto paramsDto) {
        //获取参数
        Long orderId = paramsDto.getOrderId();
        Order order = orderDao.selectById(orderId);

        /**2、更新或保存车辆信息*/
        List<ChangePriceOrderCarDto> orderCarList = paramsDto.getOrderCarList();

        //费用统计变量
        int noCount = 0;
        BigDecimal totalFee = BigDecimal.ZERO;
        for (ChangePriceOrderCarDto dto : orderCarList) {
            if (dto == null) {
                continue;
            }
            //统计数量
            noCount++;
            OrderCar orderCar = orderCarDao.selectById(dto.getId());
            if(orderCar == null){
                throw new ServerException("ID为{}的车辆，不存在", dto.getId());
            }
            //填充数据
            orderCar.setPickFee(dto.getPickFee() == null ? BigDecimal.ZERO : dto.getPickFee());
            orderCar.setTrunkFee(dto.getTrunkFee() == null ? BigDecimal.ZERO : dto.getTrunkFee());
            orderCar.setBackFee(dto.getBackFee() == null ? BigDecimal.ZERO : dto.getBackFee());
            orderCar.setAddInsuranceFee(dto.getAddInsuranceFee() == null ? BigDecimal.ZERO : dto.getAddInsuranceFee());
            orderCar.setAddInsuranceAmount(dto.getAddInsuranceAmount() == null ? 0 : dto.getAddInsuranceAmount());
            orderCarDao.updateById(orderCar);

            totalFee = orderCar.getPickFee()
                    .add(orderCar.getTrunkFee()
                            .add(orderCar.getBackFee())
                            .add(orderCar.getAddInsuranceFee()));
        }
        if(CustomerTypeEnum.COOPERATOR.code == order.getCustomerType()){
            totalFee = paramsDto.getTotalFee();
        }
        order.setTotalFee(totalFee);
        orderDao.updateById(order);

        //TODO 日志
        return BaseResultUtil.success();
    }

    /**
     * 完善订单信息
     *
     * @param paramsDto
     * @author JPG
     * @since 2019/11/5 16:51
     */
    @Override
    public ResultVo replenishInfo(ReplenishOrderDto paramsDto) {
        Order order = orderDao.selectById(paramsDto.getOrderId());
        if (order == null || order.getState() >= OrderStateEnum.TRANSPORTING.code) {
            return BaseResultUtil.fail("订单不允许修改");
        }
        List<ReplenishOrderCarDto> list = paramsDto.getOrderCarList();
        for (ReplenishOrderCarDto dto : list) {
            OrderCar orderCar = orderCarDao.selectById(dto.getId());
            orderCar.setBrand(dto.getBrand());
            orderCar.setModel(dto.getModel());
            orderCar.setPlateNo(dto.getPlateNo());
            orderCar.setVin(dto.getVin());
            orderCarDao.updateById(orderCar);
        }
        // TODO 日志
        return BaseResultUtil.success();
    }

    /**
     * 拷贝订单开始城市
     * @author JPG
     * @since 2019/11/5 9:06
     * @param vo
     * @param order
     */
    private void copyOrderStartCity(FullCity vo, Order order) {
        if(vo == null){
            return;
        }
        order.setStartProvince(vo.getProvince());
        order.setStartProvinceCode(vo.getProvinceCode());
        order.setStartCity(vo.getCity());
        order.setStartCityCode(vo.getCityCode());
        order.setStartArea(vo.getArea());
        order.setStartAreaCode(vo.getAreaCode());
    }
    /**
     * 拷贝订单结束城市
     * @author JPG
     * @since 2019/11/5 9:06
     * @param vo
     * @param order
     */
    private void copyOrderEndCity(FullCity vo, Order order) {
        if(vo == null){
            return;
        }
        order.setEndProvince(vo.getProvince());
        order.setEndProvinceCode(vo.getProvinceCode());
        order.setEndCity(vo.getCity());
        order.setEndCityCode(vo.getCityCode());
        order.setEndArea(vo.getArea());
        order.setEndAreaCode(vo.getAreaCode());
    }
    /**
     * 均摊服务费
     *
     * @param order
     * @param orderCarSavelist
     * @author JPG
     * @since 2019/10/29 8:30
     */
    private void shareTotalFee(Order order, List<OrderCar> orderCarSavelist) {
        BigDecimal totalFee = order.getTotalFee() == null ? BigDecimal.ZERO : order.getTotalFee();
        BigDecimal[] totalFeeArray = totalFee.divideAndRemainder(new BigDecimal(order.getCarNum()));
        BigDecimal totalFeeAvg = totalFeeArray[0];
        BigDecimal totalFeeRemainder = totalFeeArray[1];
        for (OrderCar orderCar : orderCarSavelist) {
            //合伙人计算均摊服务费
            if (totalFeeRemainder.compareTo(BigDecimal.ZERO) > 0) {
                orderCar.setTotalFee(totalFeeAvg.add(BigDecimal.ONE));
                totalFeeRemainder = totalFeeRemainder.subtract(BigDecimal.ONE);
            } else {
                orderCar.setTotalFee(totalFeeAvg);
            }
        }
    }

    /**
     * 均摊优惠券
     *
     * @param order
     * @param orderCarSavelist
     * @author JPG
     * @since 2019/10/29 8:27
     */
    private void shareCouponOffsetFee(Order order, List<OrderCar> orderCarSavelist) {
        BigDecimal couponOffsetFee = order.getCouponOffsetFee() == null ? BigDecimal.ZERO : order.getCouponOffsetFee();
        BigDecimal[] couponOffsetFeeArray = couponOffsetFee.divideAndRemainder(new BigDecimal(order.getCarNum()));
        BigDecimal couponOffsetFeeAvg = couponOffsetFeeArray[0];
        BigDecimal couponOffsetFeeRemainder = couponOffsetFeeArray[1];
        for (OrderCar orderCar : orderCarSavelist) {
            if (couponOffsetFeeRemainder.compareTo(BigDecimal.ZERO) > 0) {
                orderCar.setCouponOffsetFee(couponOffsetFeeAvg.add(BigDecimal.ONE));
                couponOffsetFeeRemainder = couponOffsetFeeRemainder.subtract(BigDecimal.ONE);
            } else {
                orderCar.setCouponOffsetFee(couponOffsetFeeAvg);
            }
        }
    }


}
