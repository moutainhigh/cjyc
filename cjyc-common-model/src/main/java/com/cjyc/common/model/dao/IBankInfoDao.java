package com.cjyc.common.model.dao;

import com.cjyc.common.model.entity.BankInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 银行信息对照表 Mapper 接口
 * </p>
 *
 * @author JPG
 * @since 2019-09-29
 */
public interface IBankInfoDao extends BaseMapper<BankInfo> {

    /**
     * 根据银行名称获取银行信息
     * @param bankName
     * @return
     */
    List<BankInfo> findBankInfo(@Param("bankName") String bankName);
}
