package com.cjyc.common.system.service.impl;

import com.cjyc.common.model.dao.IStoreDao;
import com.cjyc.common.model.dto.customer.freightBill.FindStoreDto;
import com.cjyc.common.model.entity.Store;
import com.cjyc.common.model.entity.defined.BizScope;
import com.cjyc.common.model.enums.BizScopeEnum;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.customer.customerLine.BusinessStoreVo;
import com.cjyc.common.model.vo.customer.customerLine.StoreListVo;
import com.cjyc.common.system.service.ICsStoreService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
public class CsStoreServiceImpl implements ICsStoreService {
    @Resource
    private IStoreDao storeDao;

    /**
     * 查询区县所属业务中心-业务范围
     *
     * @param areaCode
     * @author JPG
     * @since 2019/11/5 9:27
     */
    @Override
    public Store getOneBelongByAreaCode(String areaCode) {
        return storeDao.findOneBelongByAreaCode(areaCode);
    }

    /**
     * 根据地址城市查询业务中心
     * @author JPG
     * @since 2019/12/11 8:38
     * @param cityCode
     */
    @Override
    public Store getOneBelongByCityCode(String cityCode) {
        return storeDao.findOneBelongByCityCode(cityCode);
    }

    @Override
    public Set<Long> getStoreIds(BizScope bizScope) {
        if (bizScope.getCode() == BizScopeEnum.CHINA.code) {
            return null;
        }
        Set<Long> storeIds = bizScope.getStoreIds();
        return storeIds;
    }

    /**
     * 查询区县所属业务中心列表-业务范围
     *
     * @param areaCode
     * @author JPG
     * @since 2019/11/5 9:27
     */
    @Override
    public List<Store> getBelongByAreaCode(String areaCode) {
        return storeDao.findBelongByAreaCode(areaCode);
    }

    /**
     * 根据ID查询业务中心
     *
     * @param storeId
     * @param isSearchCache
     * @author JPG
     * @since 2019/11/5 17:26
     */
    @Override
    public Store getById(Long storeId, boolean isSearchCache) {
        return storeDao.selectById(storeId);
    }

    /**
     * 根据ID查询业务中心覆盖范围
     *
     * @param storeId
     * @return
     */
    @Override
    public List<String> getAreaBizScope(Long storeId) {
        return storeDao.findAreaBizScope(storeId);
    }

    @Override
    public List<Store> getAll() {
        return storeDao.findAll();
    }

    @Override
    public List<Long> getAllDeptId() {
        return storeDao.findAllDeptId();
    }

    /**
     * 获取所属业务中心
     * @author JPG
     * @since 2019/11/15 10:04
     * @param storeId
     * @param cityCode
     */
    @Override
    public Long getBelongStoreId(Long storeId, String cityCode) {
        if(storeId != null && storeId > 0){
            return storeId;
        }
        Store store = storeDao.findOneBelongByCityCode(cityCode);
        if(store == null){
            return null;
        }
        return storeId;
    }

    @Override
    public ResultVo<StoreListVo> findStore(FindStoreDto dto) {
        List<BusinessStoreVo> storeVos = storeDao.findStore(dto);
        StoreListVo storeVoList = new StoreListVo();
        storeVoList.setStoreVoList(storeVos);
        return BaseResultUtil.success(storeVoList);
    }



}
