package com.cjyc.web.api.controller;

import com.cjyc.common.model.dto.web.excel.OrderChangePriceExportDto;
import com.cjyc.common.model.dto.web.excel.WaybillPriceCompareExportDto;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.excel.ImportOrderChangePriceVo;
import com.cjyc.common.model.vo.web.excel.WaybillPriceCompareExportVo;
import com.cjyc.web.api.service.IExcelService;
import com.cjyc.web.api.util.ReturnExcel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "Excel")
@RestController
@CrossOrigin
@RequestMapping(value = "/excel")
public class ExcelController {

    @Resource
    private IExcelService excelService;
    @Resource
    private ReturnExcel returnExcel;


    @ApiOperation(value = "导出订单改价记录")
    @GetMapping(value = "/order/change/price/simple/export")
    public void exportOrderChangePriceSimple(OrderChangePriceExportDto reqDto, HttpServletResponse response) {
        ResultVo<List<ImportOrderChangePriceVo>> resultVo = excelService.listOrderChangePriceSimple(reqDto);
        returnExcel.printExcel(resultVo, ImportOrderChangePriceVo.class, "订单改价信息", response);
    }


    @ApiOperation(value = "导出运单价格对比信息")
    @GetMapping(value = "/waybill/car/price/compare/export")
    public void exportWaybillPriceCompare(WaybillPriceCompareExportDto reqDto, HttpServletResponse response) {
        ResultVo<List<WaybillPriceCompareExportVo>> resultVo = excelService.listWaybillPriceCompare(reqDto);
        returnExcel.printExcel(resultVo, WaybillPriceCompareExportVo.class, "运单价格对比信息", response);
    }

    /*@ApiOperation(value = "导出司机登录信息")
    @GetMapping(value = "/driver/login/count/export")
    public void exportDriverLoginCount(DriverLoginCountExportDto reqDto, HttpServletResponse response) {
        ResultVo<List<DriverLoginCountExportVo>> resultVo = excelService.ListDriverLoginCount(reqDto);
        returnExcel.printExcel(resultVo, DriverLoginCountExportVo.class, "司机登录信息", response);
    }*/


}
