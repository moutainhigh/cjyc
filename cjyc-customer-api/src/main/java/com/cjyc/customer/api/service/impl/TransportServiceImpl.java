package com.cjyc.customer.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cjyc.common.model.dao.ILineDao;
import com.cjyc.common.model.dao.IStoreDao;
import com.cjyc.common.model.dto.customer.freightBill.AreaCodeDto;
import com.cjyc.common.model.dto.customer.freightBill.TransportDto;
import com.cjyc.common.model.entity.Line;
import com.cjyc.common.model.entity.Store;
import com.cjyc.common.model.enums.ResultEnum;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.customer.customerLine.BusinessStoreVo;
import com.cjyc.customer.api.service.IInquiryService;
import com.cjyc.customer.api.service.ITransportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 *  @author: zj
 *  @Date: 2019/10/12 16:42
 *  @Description:价格相关查询
 */
@Service
@Slf4j
public class TransportServiceImpl implements ITransportService {

    @Resource
    private ILineDao lineDao;
    @Resource
    private IInquiryService inquiryService;
    @Resource
    private IStoreDao storeDao;

    @Override
    public ResultVo linePriceByCode(TransportDto dto) {
        Line line = lineDao.getLinePriceByCode(dto.getFromCode(), dto.getToCode());
        if(line == null){
            return BaseResultUtil.getVo(ResultEnum.NOEXIST_LINE.getCode(),ResultEnum.NOEXIST_LINE.getMsg());
        }
        Map<String,Object> map = new HashMap<>(10);
        map.put("defaultWlFee",line.getDefaultWlFee() == null ? BigDecimal.ZERO : line.getDefaultWlFee().divide(new BigDecimal(100)));
        inquiryService.saveInquiry(dto,line.getDefaultWlFee());
        return BaseResultUtil.success(map);
    }

    @Override
    public ResultVo findStore(AreaCodeDto dto) {
        Store store = storeDao.selectOne(new QueryWrapper<Store>().lambda()
                .eq(Store::getAreaCode, dto.getAreaCode()));
        if(store != null){
            BusinessStoreVo bsv = new BusinessStoreVo();
            bsv.setStoreId(store.getId());
            bsv.setName(store.getName());
            return BaseResultUtil.success(bsv);
        }
        return BaseResultUtil.success();
    }
}