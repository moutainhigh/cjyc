package com.cjyc.web.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjyc.common.model.dto.web.city.StoreDto;
import com.cjyc.common.model.dto.web.store.GetStoreDto;
import com.cjyc.common.model.dto.web.store.StoreAddDto;
import com.cjyc.common.model.dto.web.store.StoreQueryDto;
import com.cjyc.common.model.dto.web.store.StoreUpdateDto;
import com.cjyc.common.model.entity.Admin;
import com.cjyc.common.model.entity.Store;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.store.StoreVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 韵车业务中心信息表 服务类
 * </p>
 *
 * @author JPG
 * @since 2019-10-23
 */
public interface IStoreService_1 extends IService<Store> {

    List<Store> getByAreaCode(String areaCode);
    List<Store> getByCityCode(String areaCode);

    /**
     * 分页查询
     * @param storeQueryDto
     * @return
     */
    ResultVo queryPage(StoreQueryDto storeQueryDto);

    /**
     * 新增
     * @param storeAddDto
     * @return
     */
    ResultVo add(StoreAddDto storeAddDto);

    /**
     * 修改
     * @param storeUpdateDto
     * @return
     */
    ResultVo modify(StoreUpdateDto storeUpdateDto);

    /**
     * 导出Excel
     * @param request
     * @param response
     */
    void exportExcel(HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据业务中心id获取此机构下所有关联用户列表
     * @param storeId
     * @return
     */
    ResultVo<List<Admin>> listAdminsByStoreId(Long storeId);

    /**
     * 功能描述: 根据业务中心ID查询当前业务中心覆盖区
     * @author liuxingxiang
     * @date 2019/11/6
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo
     */
    ResultVo getStoreCoveredAreaList(StoreDto dto);

    /**
     * 功能描述: 根据业务中心ID查询当前业务中心覆盖区
     * @author liuxingxiang
     * @date 2019/11/6
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo
     */
    ResultVo getStoreNoCoveredAreaList(StoreDto dto);

    /**
     * 功能描述: 给当前业务中心新增区域
     * @author liuxingxiang
     * @date 2019/11/6
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo
     */
    ResultVo addCoveredArea(StoreDto dto);

    /**
     * 功能描述: 给当前业务中心新增区域
     * @author liuxingxiang
     * @date 2019/11/6
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo
     */
    ResultVo removeCoveredArea(StoreDto dto);

    List<Store> getListByRoleId(Long roleId);

    List<Store> get(GetStoreDto reqDto);

    List<StoreVo> getVoListByRoleId(Long roleId);

    /**
     * 功能描述: 删除业务中心
     * @author liuxingxiang
     * @date 2019/12/12
     * @param id
     * @return com.cjyc.common.model.vo.ResultVo
     */
    ResultVo remove(Long id);
}