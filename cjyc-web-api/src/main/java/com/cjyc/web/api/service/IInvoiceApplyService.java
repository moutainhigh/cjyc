package com.cjyc.web.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjyc.common.model.dto.web.invoice.InvoiceDetailAndConfirmDto;
import com.cjyc.common.model.dto.web.invoice.InvoiceQueryDto;
import com.cjyc.common.model.entity.InvoiceApply;
import com.cjyc.common.model.vo.ResultVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 发票申请信息表 服务类
 * </p>
 *
 * @author JPG
 * @since 2019-11-02
 */
public interface IInvoiceApplyService extends IService<InvoiceApply> {

    /**
     * 分页查询发票申请列表
     * @param dto
     * @return
     */
    ResultVo getInvoiceApplyPage(InvoiceQueryDto dto);

    /**
     * 查询发票信息明细
     * @param dto
     * @return
     */
    ResultVo getDetail(InvoiceDetailAndConfirmDto dto);

    /**
     * 确认开票
     * @param dto
     * @return
     */
    ResultVo confirmInvoice(InvoiceDetailAndConfirmDto dto);

    /**
     * 功能描述: 导出Excel
     * @author liuxingxiang
     * @date 2019/11/6
     * @param request
     * @param response
     * @return void
     */
    void exportExcel(HttpServletRequest request, HttpServletResponse response);
}
