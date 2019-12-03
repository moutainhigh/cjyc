package com.cjyc.web.api.service.impl;

import com.cjkj.common.utils.DateUtil;
import com.cjyc.common.model.dao.ICustomerInvoiceDao;
import com.cjyc.common.model.dao.IFinanceDao;
import com.cjyc.common.model.dao.IInvoiceReceiptDao;
import com.cjyc.common.model.dto.web.finance.ApplySettlementDto;
import com.cjyc.common.model.dto.web.finance.FinanceQueryDto;
import com.cjyc.common.model.dto.web.finance.WaitInvoiceQueryDto;
import com.cjyc.common.model.entity.CustomerInvoice;
import com.cjyc.common.model.entity.FinanceReceipt;
import com.cjyc.common.model.entity.InvoiceApply;
import com.cjyc.common.model.entity.InvoiceReceipt;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.util.RandomUtil;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.finance.FinanceReceiptVo;
import com.cjyc.common.model.vo.web.finance.FinanceVo;
import com.cjyc.common.model.vo.web.finance.TrunkLineVo;
import com.cjyc.web.api.service.ICustomerService;
import com.cjyc.web.api.service.IFinanceService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @Author:Hut
 * @Date:2019/11/21 15:06
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
public class FinanceServiceImpl implements IFinanceService {

    @Resource
    private IFinanceDao iFinanceDao;

    @Resource
    private ICustomerInvoiceDao iCustomerInvoiceDao;

    @Resource
    private IInvoiceReceiptDao iInvoiceReceiptDao;

    @Override
    public ResultVo<PageVo<FinanceVo>> getFinanceList(FinanceQueryDto financeQueryDto) {
        PageHelper.startPage(financeQueryDto.getCurrentPage(), financeQueryDto.getPageSize());
        List<FinanceVo> financeVoList = iFinanceDao.getFinanceList(financeQueryDto);
        for(int i=0;i<financeVoList.size();i++){
            FinanceVo financeVo = financeVoList.get(i);
            if(financeVo != null){
                String orderCarNo = financeVo.getNo();
                List<TrunkLineVo> trunkLineVoList = iFinanceDao.getTrunkCostList(orderCarNo);
                financeVo.setTrunkLineVoList(trunkLineVoList);
            }
        }
        PageInfo<FinanceVo> pageInfo = new PageInfo<>(financeVoList);
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public void exportExcel(HttpServletRequest request, HttpServletResponse response) {
        FinanceQueryDto financeQueryDto = getFinanceQueryDto(request);
        List<FinanceVo> financeVoList = getAllFinanceList(financeQueryDto);
    }

    private List<FinanceVo> getAllFinanceList(FinanceQueryDto financeQueryDto) {
        List<FinanceVo> financeVoList = iFinanceDao.getFinanceList(financeQueryDto);
        for(int i=0;i<financeVoList.size();i++){
            FinanceVo financeVo = financeVoList.get(i);
            if(financeVo != null){
                String orderCarNo = financeVo.getNo();
                List<TrunkLineVo> trunkLineVoList = iFinanceDao.getTrunkCostList(orderCarNo);
                financeVo.setTrunkLineVoList(trunkLineVoList);
            }
        }

        return financeVoList;
    }

    private FinanceQueryDto getFinanceQueryDto(HttpServletRequest request) {
        FinanceQueryDto financeQueryDto = new FinanceQueryDto();
        String no = request.getParameter("no");
        return financeQueryDto;
    }

    @Override
    public ResultVo<PageVo<FinanceReceiptVo>> getFinanceReceiptList(FinanceQueryDto financeQueryDto) {
        PageHelper.startPage(financeQueryDto.getCurrentPage(), financeQueryDto.getPageSize());
        List<FinanceReceiptVo> financeVoList = iFinanceDao.getFinanceReceiptList(financeQueryDto);
        PageInfo<FinanceReceiptVo> pageInfo = new PageInfo<>(financeVoList);
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public void applySettlement(ApplySettlementDto applySettlementDto) {
        int invoiceId = 0;
        String state="0";
        if(applySettlementDto != null && applySettlementDto.getIsInvoice() != null && applySettlementDto.getIsInvoice().equals("1")){
            CustomerInvoice customerInvoice = new CustomerInvoice();
            customerInvoice.setCustomerId(applySettlementDto.getCustomerId());
            customerInvoice.setType(applySettlementDto.getType());
            customerInvoice.setName(applySettlementDto.getCustomerName());
            customerInvoice.setTaxCode(applySettlementDto.getTaxPayerNumber());
            customerInvoice.setInvoiceAddress(applySettlementDto.getAddress());
            customerInvoice.setTel(applySettlementDto.getPhone());
            customerInvoice.setBankName(applySettlementDto.getBankName());
            customerInvoice.setBankAccount(applySettlementDto.getBankAccout());
            customerInvoice.setPickupPerson(applySettlementDto.getAddressee());
            customerInvoice.setPickupPhone(applySettlementDto.getAddresseePhone());
            customerInvoice.setPickupAddress(applySettlementDto.getMailAddress());
            invoiceId = iCustomerInvoiceDao.insert(customerInvoice);
            state="1";
        }

        InvoiceReceipt invoiceReceipt = new InvoiceReceipt();
        invoiceReceipt.setOrderCarNo(applySettlementDto.getNo());
        invoiceReceipt.setCustomerId(applySettlementDto.getCustomerId());
        invoiceReceipt.setAmount(applySettlementDto.getAmount().multiply(new BigDecimal(100)));
        invoiceReceipt.setInvoiceId(Long.valueOf(invoiceId));
        invoiceReceipt.setState(state);
        invoiceReceipt.setInvoiceTime(System.currentTimeMillis());

        StringBuilder builder = new StringBuilder("S");
        builder.append(String.valueOf(DateUtil.format(new Date(),"YYMMdd")));
        builder.append(RandomUtil.getMathRandom(6));
        invoiceReceipt.setSerialNumber(builder.toString());

        iInvoiceReceiptDao.insert(invoiceReceipt);
    }

    @Override
    public void confirmSettlement(String orderCarNo) {

    }

    @Override
    public ResultVo<PageVo<FinanceReceiptVo>> getWaitInvoiceList(WaitInvoiceQueryDto waitInvoiceQueryDto) {

        PageHelper.startPage(waitInvoiceQueryDto.getCurrentPage(), waitInvoiceQueryDto.getPageSize());
        //iInvoiceReceiptDao.getWaitInvoiceList(waitInvoiceQueryDto);

        return null;
    }
}