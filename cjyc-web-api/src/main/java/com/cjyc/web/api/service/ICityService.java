package com.cjyc.web.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjyc.common.model.dto.KeywordDto;
import com.cjyc.common.model.dto.salesman.city.CityPageDto;
import com.cjyc.common.model.dto.web.city.CityQueryDto;
import com.cjyc.common.model.entity.City;
import com.cjyc.common.model.vo.CityTreeVo;
import com.cjyc.common.model.vo.ResultVo;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 韵车城市信息表 服务类
 * </p>
 *
 * @author JPG
 * @since 2019-09-30
 */
public interface ICityService extends IService<City> {


    /**
     * 根据code查询城市
     * @author JPG
     * @since 2019/10/12 11:41
     * @param cityCode
     */
    City findById(@Param("cityCode") String cityCode);

    /**
     * 根据列查询城市列表
     * @author JPG
     * @since 2019/10/12 11:40
     * @param level
     */
    List<City> selectList(int level);

    IPage<City> selectPage(CityPageDto cityPageDto);

    /**
     * 查询下属城市列表
     * @author JPG
     * @since 2019/10/12 13:31
     * @param code
     */
    List<City> selectChildList(String code);

    /**
     *  根据起始目的节点查询树形结构
     * @param startLevel
     * @param endLevel
     * @return
     */
    ResultVo<List<CityTreeVo>> cityTree(Integer startLevel, Integer endLevel);

    /**
     * 根据关键字搜索省城市树形结构
     * @param keyword
     * @return
     */
    ResultVo<List<CityTreeVo>> keywordCityTree(String keyword);

    /**
     * 功能描述: 分页查询省，城市，区
     * @author liuxingxiang
     * @date 2019/11/6
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo
     */
    ResultVo getCityPage(CityQueryDto dto);

    /**
     * 功能描述: 根据省名称查询省份信息
     * @author liuxingxiang
     * @date 2019/11/7
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo<java.util.List<com.cjyc.common.model.entity.City>>
     */
    ResultVo<List<City>> getProvinceList(KeywordDto dto);

    /**
     *  城市管理导出
     * @param request
     * @param response
     */
    void exportCityListExcel(HttpServletRequest request, HttpServletResponse response);
}
