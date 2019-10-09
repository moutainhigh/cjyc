package com.cjyc.common.model.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjyc.common.model.enums.ResultEnum;
import com.cjyc.common.model.vo.ListVo;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultVo;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * 封装返回数据工具
 * @author JPG
 */
public class BaseResultUtil<T> {

    /**
     * 快速返回成功
     * @author JPG
     * @since 2019/10/9 11:49
     * @param
     * @return
     */
    public static <T> ResultVo<T> success(){
        return success(null);
    }

    public static <T> ResultVo<T> success(T data){
        return getVo(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMsg(), data);
    }

    /**
     * 快速返回失败
     * @author JPG
     * @since 2019/10/9 11:49
     * @param
     * @return
     */
    public static <T> ResultVo<T> fail(){
        return fail(null);
    }
    public static <T> ResultVo<T> fail(T data){
        return getVo(ResultEnum.FAIL.getCode(), ResultEnum.FAIL.getMsg(), data);
    }

    /**
     * 快速返回参数错误
     * @author JPG
     * @since 2019/10/9 11:49
     * @param
     * @return
     */
    public static <T> ResultVo<T> paramError(){
        return paramError(ResultEnum.MOBILE_PARAM_ERROR.getMsg());
    }
    public static <T> ResultVo<T> paramError(String message){
        return getVo(ResultEnum.MOBILE_PARAM_ERROR.getCode(), message);
    }

    /**
     * 快速返回服务器错误
     * @author JPG
     * @since 2019/10/9 11:49
     * @param
     * @return
     */
    public static <T> ResultVo<T> serverError(){
        return serverError(ResultEnum.API_INVOKE_ERROR.getMsg());
    }
    public static <T> ResultVo<T> serverError(String message){
        return getVo(ResultEnum.API_INVOKE_ERROR.getCode(), message);
    }


    /**
     * 获取ResultVo<T>(无内容)
     * @author JPG
     * @date 2019/7/31 9:47
     * @param code 返回码
     * @param message 返回信息
     * @return ResultVo
     */
    public static<T> ResultVo<T> getVo(int code, String message){
        return getVo(code, message, null);
    }

    /**
     * 获取ResultVo<T>
     * @author JPG
     * @date 2019/7/31 10:02
     * @param code 返回码
     * @param message 返回信息
     * @param data 返回内容
     * @return ResultVo
     */
    public static <T> ResultVo<T> getVo(int code, String message, T data){
        return ResultVo.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 获取ResultVo<ListVo<T>>
     * @description 结果无分页，不带分页相关统计信息，带非分页相关信息集合countInfo
     * @author JPG
     * @date 2019/7/31 10:46
     * @param code 返回码
     * @param message 返回信息
     * @param list 返回内容
     * @param countInfo 非分页相关统计信息
     * @return  ResultVo<ListVo<T>>
     */
    public static <T> ResultVo<ListVo<T>> getListVo(int code, String message, List<T> list, Map<String, Object> countInfo){
        Long totalRecords = null;
        if(countInfo != null){
            try {
                totalRecords = countInfo.get("totalCount") == null ? null : (long)countInfo.get("totalCount");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        ListVo<T> listVo = ListVo.<T>builder()
                .totalRecords(totalRecords)
                .list(list)
                .countInfo(countInfo)
                .build();
        return ResultVo.<ListVo<T>>builder()
                .code(code)
                .message(message)
                .data(listVo)
                .build();

    }

    /**
     * 获取ResultVo<PageVo<T>>
     * @description 结果带分页(PageHelper)，带分页相关统计信息
     * @author JPG
     * @date 2019/7/31 10:23
     * @param code 返回码
     * @param message 返回信息
     * @param pageInfo 返回内容
     * @return ResultVo<PageVo<T>>
     */
    public static <T> ResultVo<PageVo<T>> getPageVo(int code, String message, PageInfo<T> pageInfo){
        PageVo<T> pageVo = PageVo.<T>builder()
                .totalRecords(pageInfo.getTotal())
                .totalPages(pageInfo.getPages())
                .currentPage(pageInfo.getPageNum())
                .pageSize(pageInfo.getSize())
                .list(pageInfo.getList())
                .build();

        return ResultVo.<PageVo<T>>builder()
                .code(code)
                .message(message)
                .data(pageVo)
                .build();

    }

    /**
     * 获取ResultVo<PageVo<T>>
     * @description 结果带分页(PageHelper)，带分页相关统计信息，带非分页相关信息集合countInfo
     * @author JPG
     * @date 2019/7/31 10:25
     * @param code 返回码
     * @param message 返回信息
     * @param pageInfo 返回内容
     * @param countInfo 非分页相关统计信息
     * @return ResultVo<PageVo<T>>
     */
    public static <T> ResultVo<PageVo<T>> getPageVo(int code, String message, PageInfo<T> pageInfo, Map<String, Object> countInfo){
        PageVo<T> pageVo = PageVo.<T>builder()
                .totalRecords(pageInfo.getTotal())
                .totalPages(pageInfo.getPages())
                .currentPage(pageInfo.getPageNum())
                .pageSize(pageInfo.getSize())
                .list(pageInfo.getList())
                .countInfo(countInfo)
                .build();
        return ResultVo.<PageVo<T>>builder()
                .code(code)
                .message(message)
                .data(pageVo)
                .build();

    }

    /**
     * 获取ResultVo<PageVo<T>>
     * @author JPG
     * @date 2019/7/31 14:09
     * @param code 返回码
     * @param message 返回信息
     * @param page 返回内容
     * @return ResultVo<PageVo<T>>
     */
    public static <T> ResultVo<PageVo<T>> getPageVo(int code, String message, Page<T> page){
        PageVo<T> pageVo = PageVo.<T>builder()
                .totalRecords(page.getTotal())
                .totalPages((int)page.getPages())
                .currentPage((int)page.getCurrent())
                .pageSize((int)page.getSize())
                .list(page.getRecords())
                .build();
        return ResultVo.<PageVo<T>>builder()
                .code(code)
                .message(message)
                .data(pageVo)
                .build();

    }
    /**
     * 获取ResultVo<PageVo<T>>
     * @description 结果带分页(Mybatis-plus)，带分页相关统计信息，带非分页相关信息集合countInfo
     * @author JPG
     * @date 2019/7/31 14:09
     * @param code 返回码
     * @param message 返回信息
     * @param page 返回内容
     * @param countInfo 非分页相关统计信息
     * @return ResultVo<PageVo<T>>
     */
    public static <T> ResultVo<PageVo<T>> getPageVo(int code, String message, Page<T> page, Map<String, Object> countInfo){
        PageVo<T> pageVo = PageVo.<T>builder()
                .totalRecords(page.getTotal())
                .totalPages((int)page.getPages())
                .currentPage((int)page.getCurrent())
                .pageSize((int)page.getSize())
                .list(page.getRecords())
                .countInfo(countInfo)
                .build();
        return ResultVo.<PageVo<T>>builder()
                .code(code)
                .message(message)
                .data(pageVo)
                .build();

    }

 }
