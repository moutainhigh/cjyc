package com.cjyc.customer.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjyc.common.model.entity.Admin;

/**
 * <p>
 * 韵车后台管理员表 服务类
 * </p>
 *
 * @author JPG
 * @since 2019-10-09
 */
public interface IAdminService extends IService<Admin> {

    Admin getByUserId(Long userId);
}
