package com.cjyc.web.api.controller;

import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.web.api.config.YQZLProperty;
import com.yqzl.constant.EnumBankProCode;
import com.yqzl.model.request.FundTransferRequest;
import com.yqzl.service.FundBankClient;
import com.yqzl.service.FundBankClientFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 银企直联：提供银行转账，账户查询，交易流水查询等功能
 *
 * @author RenPL
 */
@RestController
@Api(tags = "银企直联")
@RequestMapping(value = "/yqzl", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class YqzlController {

    @Autowired
    private FundBankClientFactory fundBankClientFactory;

    @ApiOperation(value = "银企直联-转账交易")
    @PostMapping(value = "/transfer")
    public ResultVo doTransfer(FundTransferRequest fundTransferRequest) {
        // 根据银行渠道信息找银行产品实现类，找不到就异常
        FundBankClient bankOperations = fundBankClientFactory.get(fundTransferRequest.getBankProCode());
        // 交通银行
        fundTransferRequest.setBankProCode(EnumBankProCode.COMM_BANK.name());
        // 企业代码
        fundTransferRequest.setCorpNo(YQZLProperty.corpNo);
        // 企业用户号
        fundTransferRequest.setUserNo(YQZLProperty.userNo);
        return bankOperations.doTransfer(fundTransferRequest);
    }
}