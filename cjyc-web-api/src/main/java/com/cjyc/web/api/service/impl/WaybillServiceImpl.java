package com.cjyc.web.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjkj.common.utils.ExcelUtil;
import com.cjyc.common.model.dao.ICarrierDao;
import com.cjyc.common.model.dao.IWaybillCarDao;
import com.cjyc.common.model.dao.IWaybillDao;
import com.cjyc.common.model.dto.web.waybill.*;
import com.cjyc.common.model.entity.Carrier;
import com.cjyc.common.model.entity.Waybill;
import com.cjyc.common.model.entity.defined.BizScope;
import com.cjyc.common.model.enums.BizScopeEnum;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.customer.PartnerExportExcel;
import com.cjyc.common.model.vo.web.waybill.*;
import com.cjyc.common.system.service.ICsWaybillService;
import com.cjyc.common.system.service.sys.ICsSysService;
import com.cjyc.web.api.service.IWaybillService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 运单表(业务员调度单) 服务实现类
 * </p>
 *
 * @author JPG
 * @since 2019-10-17
 */
@Service
@Transactional(rollbackFor = RuntimeException.class)
public class WaybillServiceImpl extends ServiceImpl<IWaybillDao, Waybill> implements IWaybillService {

    @Resource
    private IWaybillDao waybillDao;
    @Resource
    private IWaybillCarDao waybillCarDao;
    @Resource
    private ICsWaybillService csWaybillService;
    @Resource
    private ICsSysService csSysService;
    @Resource
    private ICarrierDao carrierDao;


    @Override
    public ResultVo<List<HistoryListWaybillVo>> historyList(HistoryListDto paramsDto) {
        List<HistoryListWaybillVo> list = waybillDao.findHistoryList(paramsDto);
        return BaseResultUtil.success(list);
    }

    @Override
    public ResultVo<PageVo<CrWaybillVo>> crListForMineCarrier(CrWaybillDto paramsDto) {
        paramsDto.setCarrierId(paramsDto.getCarrierId());
        //查询承运商信息
        PageHelper.startPage(paramsDto.getCurrentPage(), paramsDto.getPageSize(), true);
        List<CrWaybillVo> list = waybillDao.findCrListForMineCarrier(paramsDto);
        PageInfo<CrWaybillVo> pageInfo = new PageInfo<>(list);
        if(paramsDto.getCurrentPage() > pageInfo.getPages()){
            pageInfo.setList(null);
        }
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public ResultVo<PageVo<LocalListWaybillCarVo>> locallist(LocalListWaybillCarDto paramsDto) {
        //查询角色业务中心范围
        BizScope bizScope = csSysService.getBizScopeBySysRoleIdNew(paramsDto.getLoginId(), paramsDto.getRoleId(), true);
        if(bizScope == null || bizScope.getCode() == BizScopeEnum.NONE.code){
            return BaseResultUtil.fail("没有数据权限");
        }
        if(bizScope.getCode() == BizScopeEnum.CHINA.code){
            paramsDto.setLoginId(null);
        }
        paramsDto.setBizScope(bizScope.getStoreIds());
        PageHelper.startPage(paramsDto.getCurrentPage(), paramsDto.getPageSize(), true);
        List<LocalListWaybillCarVo> list = waybillCarDao.findListLocal(paramsDto);
        PageInfo<LocalListWaybillCarVo> pageInfo = new PageInfo<>(list);
        if(paramsDto.getCurrentPage() > pageInfo.getPages()){
            pageInfo.setList(null);
        }
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public List<LocalListWaybillCarVo> localAllList(LocalListWaybillCarDto reqDto) {
        return waybillCarDao.findListLocal(reqDto);
    }

    @Override
    public ResultVo<PageVo<TrunkMainListWaybillVo>> getTrunkMainList(TrunkMainListWaybillDto paramsDto) {
        //查询角色业务中心范围
        BizScope bizScope = csSysService.getBizScopeBySysRoleIdNew(paramsDto.getLoginId(), paramsDto.getRoleId(), true);
        if(bizScope == null || bizScope.getCode() == BizScopeEnum.NONE.code){
            return BaseResultUtil.fail("没有数据权限");
        }
        if(bizScope.getCode() == BizScopeEnum.CHINA.code){
            paramsDto.setLoginId(null);
        }
        paramsDto.setBizScope(bizScope.getStoreIds());
        PageHelper.startPage(paramsDto.getCurrentPage(), paramsDto.getPageSize(), true);
        List<TrunkMainListWaybillVo> list = waybillDao.findMainListTrunk(paramsDto);
        PageInfo<TrunkMainListWaybillVo> pageInfo = new PageInfo<>(list);
        if(paramsDto.getCurrentPage() > pageInfo.getPages()){
            pageInfo.setList(null);
        }
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public List<TrunkMainListWaybillVo> getTrunkMainAllList(TrunkMainListWaybillDto reqDto) {
        return waybillDao.findMainListTrunk(reqDto);
    }

    @Override
    public ResultVo<PageVo<TrunkSubListWaybillVo>> getTrunkSubList(TrunkSubListWaybillDto paramsDto) {
        //查询角色业务中心范围
        BizScope bizScope = csSysService.getBizScopeBySysRoleIdNew(paramsDto.getLoginId(), paramsDto.getRoleId(), true);
        if(bizScope == null || bizScope.getCode() == BizScopeEnum.NONE.code){
            return BaseResultUtil.fail("没有数据权限");
        }
        if(bizScope.getCode() == BizScopeEnum.CHINA.code){
            paramsDto.setLoginId(null);
        }
        paramsDto.setBizScope(bizScope.getStoreIds());
        PageHelper.startPage(paramsDto.getCurrentPage(), paramsDto.getPageSize(), true);
        List<TrunkSubListWaybillVo> list = waybillDao.findSubListTrunk(paramsDto);
        PageInfo<TrunkSubListWaybillVo> pageInfo = new PageInfo<>(list);
        if(paramsDto.getCurrentPage() > pageInfo.getPages()){
            pageInfo.setList(null);
        }
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public ResultVo<List<TrunkSubListExportVo>> getTrunkSubAllList(TrunkSubListWaybillDto reqDto) {
        //查询角色业务中心范围
        BizScope bizScope = csSysService.getBizScopeBySysRoleIdNew(reqDto.getLoginId(), reqDto.getRoleId(), true);
        if(bizScope == null || bizScope.getCode() == BizScopeEnum.NONE.code){
            return BaseResultUtil.fail("没有数据权限");
        }
        if(bizScope.getCode() == BizScopeEnum.CHINA.code){
            reqDto.setLoginId(null);
        }
        List<TrunkSubListWaybillVo> list = waybillDao.findSubListTrunk(reqDto);
        if (CollectionUtils.isEmpty(list)) {
            return BaseResultUtil.success();
        }else {
            List<TrunkSubListExportVo> rsList = new ArrayList<>(list.size());
            list.forEach(l -> {
                TrunkSubListExportVo vo = new TrunkSubListExportVo();
                BeanUtils.copyProperties(l, vo);
                rsList.add(vo);
            });
            return BaseResultUtil.success(rsList);
        }
    }


    @Override
    public ResultVo<PageVo<TrunkCarListWaybillCarVo>> trunkCarlist(TrunkListWaybillCarDto paramsDto) {
        //查询角色业务中心范围
        BizScope bizScope = csSysService.getBizScopeBySysRoleIdNew(paramsDto.getLoginId(), paramsDto.getRoleId(), true);
        if(bizScope == null || bizScope.getCode() == BizScopeEnum.NONE.code){
            return BaseResultUtil.fail("无数据权限");
        }
        if(bizScope.getCode() == BizScopeEnum.CHINA.code){
            paramsDto.setBizScope(null);
            paramsDto.setLoginId(null);
        }else{
            paramsDto.setBizScope(bizScope.getStoreIds());
        }


        PageHelper.startPage(paramsDto.getCurrentPage(), paramsDto.getPageSize(), true);
        List<TrunkCarListWaybillCarVo> list = waybillCarDao.findTrunkCarList(paramsDto);
        PageInfo<TrunkCarListWaybillCarVo> pageInfo = new PageInfo<>(list);
        if(paramsDto.getCurrentPage() > pageInfo.getPages()){
            pageInfo.setList(null);
        }
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public ResultVo<List<TrunkCarDetailExportVo>> trunkCarAllList(TrunkListWaybillCarDto dto) {
        //查询角色业务中心范围
        BizScope bizScope = csSysService.getBizScopeBySysRoleIdNew(dto.getLoginId(), dto.getRoleId(), true);
        if(bizScope == null || bizScope.getCode() == BizScopeEnum.NONE.code){
            return BaseResultUtil.fail("无数据权限");
        }
        if(bizScope.getCode() == BizScopeEnum.CHINA.code){
            dto.setBizScope(null);
            dto.setLoginId(null);
        }else{
            dto.setBizScope(bizScope.getStoreIds());
        }
        List<TrunkCarListWaybillCarVo> list = waybillCarDao.findTrunkCarList(dto);
        if (CollectionUtils.isEmpty(list)) {
            return BaseResultUtil.success();
        }else {
            List<TrunkCarDetailExportVo> rsList = new ArrayList<>(list.size());
            list.stream().forEach(l -> {
                TrunkCarDetailExportVo vo = new TrunkCarDetailExportVo();
                BeanUtils.copyProperties(l, vo);
                rsList.add(vo);
            });
            return BaseResultUtil.success(rsList);
        }
    }

    @Override
    public ResultVo<WaybillVo> get(Long waybillId) {
        WaybillVo waybillVo = waybillDao.findVoById(waybillId);
        List<WaybillCarVo> waybillCarVo = waybillCarDao.findVoByWaybillId(waybillId);
        if(waybillCarVo != null){
            waybillVo.setList(waybillCarVo);
        }
        return BaseResultUtil.success(waybillVo);
    }

    @Override
    public ResultVo<WaybillVo> get(GetDto paramsDto) {
        WaybillVo waybillVo = waybillDao.findVoById(paramsDto.getWaybillId());
        List<WaybillCarVo> voList = waybillCarDao.findVo(paramsDto);
        if(!CollectionUtils.isEmpty(voList)){
            waybillVo.setList(voList);
        }
        return BaseResultUtil.success(waybillVo);
    }

    @Override
    public ResultVo<List<WaybillCarTransportVo>> getCarByType(Long orderCarId, Integer waybillType) {
       List<WaybillCarTransportVo> list =waybillCarDao.findVoByType(orderCarId, waybillType);
        return BaseResultUtil.success(list);
    }



    /************************************韵车集成改版 st***********************************/
    @Override
    public ResultVo<PageVo<CrWaybillVo>> crListForMineCarrierNew(CrWaybillDto paramsDto) {
        //根据角色查询承运商ID
        Carrier carrier = carrierDao.selectById(paramsDto.getCarrierId());
        if(carrier == null){
            return BaseResultUtil.fail("承运商信息不存在");
        }
        paramsDto.setCarrierId(carrier.getId());
        //查询承运商信息
        PageHelper.startPage(paramsDto.getCurrentPage(), paramsDto.getPageSize(), true);
        List<CrWaybillVo> list = waybillDao.findCrListForMineCarrier(paramsDto);
        PageInfo<CrWaybillVo> pageInfo = new PageInfo<>(list);
        if(paramsDto.getCurrentPage() > pageInfo.getPages()){
            pageInfo.setList(null);
        }
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public void exportCrListExcel(HttpServletRequest request, HttpServletResponse response) {
        CrWaybillDto dto = findCrWaybillDto(request);
        List<CrWaybillVo> crWaybillVoList = waybillDao.findCrListForMineCarrier(dto);
        List<ExportCrWaybillVo> exportExcelList = Lists.newArrayList();
        for (CrWaybillVo vo : crWaybillVoList) {
            ExportCrWaybillVo exportCrWaybillVo = new ExportCrWaybillVo();
            BeanUtils.copyProperties(vo, exportCrWaybillVo);
            exportExcelList.add(exportCrWaybillVo);
        }
        String title = "我的运单";
        String sheetName = "我的运单";
        String fileName = "我的运单.xls";
        try {
            ExcelUtil.exportExcel(exportExcelList, title, sheetName, ExportCrWaybillVo.class, fileName, response);
        } catch (IOException e) {
            log.error("导出我的运单信息异常:{}",e);
        }
    }

    /**
     * 封装运单excel请求
     * @param request
     * @return
     */
    private CrWaybillDto findCrWaybillDto(HttpServletRequest request){
        CrWaybillDto dto = new CrWaybillDto();
        dto.setWaybillNo(request.getParameter("waybillNo"));
        dto.setCarrierId(StringUtils.isBlank(request.getParameter("carrierId")) ? null:Long.valueOf(request.getParameter("carrierId")));
        return dto;
    }

}
