package com.cjyc.web.api.service;

import com.cjyc.common.model.dto.web.finance.CooperatorSearchDto;
import com.cjyc.common.model.dto.web.finance.FinanceQueryDto;
import com.cjyc.common.model.dto.web.finance.PayMentQueryDto;

import java.math.BigDecimal;

/**
 * @Author: Hut
 * @Date: 2020/03/16 9:34
 **/
public interface ITradeBillSummaryService {

    /**
     * 收入汇总
     * @return
     * @param financeQueryDto
     */
    BigDecimal incomeSummary(FinanceQueryDto financeQueryDto);

    /**
     * 退款汇总
     * @return
     */
    BigDecimal refundSummary(FinanceQueryDto financeQueryDto);

    /**
     * 成本汇总
     * @return
     * @param financeQueryDto
     */
    BigDecimal costSummary(FinanceQueryDto financeQueryDto);

    /**
     * 毛利
     * @return
     * @param financeQueryDto
     */
    BigDecimal grossProfit(FinanceQueryDto financeQueryDto);

    /**
     * 应收账款汇总
     * @param financeQueryDto
     * @return
     */
    BigDecimal receiptSummary(FinanceQueryDto financeQueryDto);

    BigDecimal payToCarrierSummary(PayMentQueryDto payMentQueryDto);

    BigDecimal paidToCarrierSummary(PayMentQueryDto payMentQueryDto);

    BigDecimal payToCooperatorSummary(CooperatorSearchDto cooperatorSearchDto);

    BigDecimal paidToCooperatorSummary(CooperatorSearchDto cooperatorSearchDto);
}