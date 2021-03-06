package com.cjyc.salesman.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjyc.common.model.dao.*;
import com.cjyc.common.model.dto.salesman.order.SalesOrderDetailDto;
import com.cjyc.common.model.dto.salesman.order.SalesOrderQueryDto;
import com.cjyc.common.model.entity.*;
import com.cjyc.common.model.entity.defined.BizScope;
import com.cjyc.common.model.enums.BizScopeEnum;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.util.JsonUtils;
import com.cjyc.common.model.util.TimeStampUtil;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.salesman.order.SalesOrderCarVo;
import com.cjyc.common.model.vo.salesman.order.SalesOrderDetailVo;
import com.cjyc.common.model.vo.salesman.order.SalesOrderVo;
import com.cjyc.common.model.vo.web.OrderCarVo;
import com.cjyc.common.system.config.LogoImgProperty;
import com.cjyc.common.system.service.ICsAdminService;
import com.cjyc.common.system.service.sys.ICsSysService;
import com.cjyc.salesman.api.service.IOrderService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<IOrderDao, Order> implements IOrderService {

    @Resource
    private IOrderDao orderDao;
    @Resource
    private IOrderCarDao orderCarDao;
    @Resource
    private ICarSeriesDao carSeriesDao;
    @Resource
    private ICustomerDao customerDao;
    @Resource
    private ILineDao lineDao;
    @Resource
    private ICustomerContractDao contractDao;
    @Resource
    private ICsSysService csSysService;
    @Resource
    private ICsAdminService csAdminService;

    @Override
    public ResultVo<PageVo<SalesOrderVo>> findOrder(SalesOrderQueryDto dto) {
        log.info("下单/接单/全部列表请求json数据 :: "+ JsonUtils.objectToJson(dto));
        // 根据登录ID查询当前业务员所在业务中心ID
        BizScope bizScope = csSysService.getBizScopeByLoginIdNew(dto.getLoginId(), true);
        // 判断当前登录人是否有权限访问
        if (BizScopeEnum.NONE.code == bizScope.getCode()) {
            return BaseResultUtil.fail("您没有访问权限!");
        }
        // 获取业务中心ID
        dto.setStoreIds(bizScope.getStoreIds());
        if(dto.getCreateEndTime() != null){
            dto.setCreateEndTime(TimeStampUtil.addDays(dto.getCreateEndTime(),1));
        }
        if(dto.getExpectEndTime() != null){
            dto.setExpectEndTime(TimeStampUtil.addDays(dto.getExpectEndTime(),1));
        }
        PageHelper.startPage(dto.getCurrentPage(),dto.getPageSize());
        List<SalesOrderVo> orderVos = orderDao.findOrder(dto);
        PageInfo<SalesOrderVo> pageInfo = new PageInfo(orderVos);
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public ResultVo<SalesOrderDetailVo> findOrderDetail(SalesOrderDetailDto dto) {
        SalesOrderDetailVo detailVo = new SalesOrderDetailVo();
        List<SalesOrderCarVo> carVoList = Lists.newArrayList();
        Order order = orderDao.selectById(dto.getOrderId());
        if(order == null){
            return BaseResultUtil.success(detailVo);
        }
        Customer customer = customerDao.findByUserId(order.getCreateUserId());
        if(customer != null){
            //客户下单
            detailVo.setFlag(1);
        }else{
            //业务员下单
            detailVo.setFlag(2);
        }
        //查看大客户合同
        if(order.getCustomerContractId() != null){
            CustomerContract contract = contractDao.selectById(order.getCustomerContractId());
            if(contract != null){
                detailVo.setContractNo(contract.getContractNo());
                detailVo.setSettlePeriod(contract.getSettlePeriod());
            }
        }
        detailVo.setOrderId(order.getId());
        detailVo.setOrderNo(order.getNo());
        detailVo.setName(order.getCustomerName());
        BeanUtils.copyProperties(order,detailVo);
        List<OrderCar> orderCars = orderCarDao.selectList(new QueryWrapper<OrderCar>().lambda().eq(OrderCar::getOrderId, order.getId()));
        String logoImg = LogoImgProperty.logoImg;
        BigDecimal totalPickFee = BigDecimal.ZERO;
        BigDecimal totalBackFee = BigDecimal.ZERO;
        BigDecimal totalAddInsuranceFee = BigDecimal.ZERO;
        BigDecimal totalTrunkFee = BigDecimal.ZERO;
        if(!CollectionUtils.isEmpty(orderCars)){
            for(OrderCar orderCar : orderCars){
                String carLogoImg = carSeriesDao.getLogoImgByBraMod(orderCar.getBrand(), orderCar.getModel());
                SalesOrderCarVo carVo = new SalesOrderCarVo();
                carVo.setOrderCarId(orderCar.getId());
                carVo.setOrderCarNo(orderCar.getNo());
                carVo.setLogoImg(logoImg+carLogoImg);
                BeanUtils.copyProperties(orderCar,carVo);
                carVoList.add(carVo);
                totalPickFee = totalPickFee.add(orderCar.getPickFee());
                totalBackFee = totalBackFee.add(orderCar.getBackFee());
                totalAddInsuranceFee = totalAddInsuranceFee.add(orderCar.getAddInsuranceFee());
                totalTrunkFee = totalTrunkFee.add(orderCar.getTrunkFee());
            }
        }
        Line line = lineDao.getLinePriceByCode(order.getStartCityCode(), order.getEndCityCode());
        detailVo.setDefaultWlFee(line.getDefaultWlFee() == null ? BigDecimal.ZERO : line.getDefaultWlFee());
        detailVo.setTotalPickFee(totalPickFee);
        detailVo.setTotalBackFee(totalBackFee);
        detailVo.setTotalAddInsuranceFee(totalAddInsuranceFee);
        detailVo.setTotalTrunkFee(totalTrunkFee);
        detailVo.setCarVoList(carVoList);
        return BaseResultUtil.success(detailVo);
    }

    @Override
    public OrderCarVo getCarVoById(Long orderCarId) {

        OrderCarVo vo = orderCarDao.findVoById(orderCarId);
        Admin admin = csAdminService.findLoop(vo.getEndStoreId());
        if(admin != null){
            vo.setEndStoreLooplinkUserId(admin.getId());
            vo.setEndStoreLooplinkName(admin.getName());
            vo.setEndStoreLooplinkPhone(admin.getPhone());
        }
        return vo;
    }

}