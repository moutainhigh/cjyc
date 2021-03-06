package com.cjyc.common.system.service;
import com.Pingxx.model.Order;
import com.cjyc.common.model.entity.TradeBill;
import com.cjyc.common.model.vo.ResultVo;
import com.pingplusplus.model.Charge;
import com.pingplusplus.model.Event;
import com.pingplusplus.model.Transfer;

import java.math.BigDecimal;
import java.util.List;
/**
 * @Author: Hut
 * @Date: 2020/03/07 15:49
 */
public interface ICsTransactionService {
    void saveTransactions(Object obj, String state);

    void cancelOrderRefund(String orderCode);

    void updateTransactions(Order object, Event event, String s);

    TradeBill getTradeBillByOrderNo(String orderNo);

    BigDecimal getAmountByOrderNo(String orderNo);

    BigDecimal getAmountByOrderCarNos(List<String> orderCarNos);

    int save(Object obj);

    List<String> getOrderCarNosByTaskId(Long taskId);

    List<String> getOrderCarNosByTaskCarIds(List<Long> taskCarIdList);

    TradeBill getTradeBillByOrderNoAndType(String no,int type);

    void updateWayBillPayStateNoPay(Long waybillId,long time);

    BigDecimal getAmountByOrderCarNosToPartner(List<String> orderCarNosList);

    void saveCooperatorTransactions(Transfer transfer, String s);

    void saveWebPrePayTransactions(Charge charge, String s);

    void saveSalesPrePayTransactions(Charge charge, String s);

    void updateOrderFlag(String orderNo, String state, long l);

    BigDecimal getWlFeeCount(Long carrierId);

    BigDecimal getCooperatorServiceFeeCount(Long customId);

    BigDecimal getCooperatorServiceReceiveFeeCount(Long customId);

    BigDecimal getCooperatorServiceFeeCarCount(Long customId);

    BigDecimal getCooperatorServiceReceiveCarFeeCount(Long customId);

    BigDecimal getReceiveFeeCount(Long carrierId);

    void payToCooperator();

    void getPayingOrder();

    ResultVo updateFailOrder(String orderNo);

    List<TradeBill> getTradeBillList(String no, int code);
}
