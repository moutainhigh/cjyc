package com.cjyc.common.system.service.impl;

import com.cjkj.common.model.ResultData;
import com.cjkj.common.redis.template.StringRedisUtil;
import com.cjkj.usercenter.dto.common.SelectDeptResp;
import com.cjkj.usercenter.dto.common.SelectRoleResp;
import com.cjyc.common.model.dao.IAdminDao;
import com.cjyc.common.model.entity.Admin;
import com.cjyc.common.model.enums.ClientEnum;
import com.cjyc.common.model.keys.RedisKeys;
import com.cjyc.common.model.vo.web.admin.CacheAdminVo;
import com.cjyc.common.system.feign.ISysDeptService;
import com.cjyc.common.system.feign.ISysRoleService;
import com.cjyc.common.system.service.ICsAdminService;
import com.cjyc.common.system.service.sys.ICsSysService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 业务员公用业务
 *
 * @author JPG
 */
@Service
public class CsAdminServiceImpl implements ICsAdminService {
    @Resource
    private IAdminDao adminDao;
    @Resource
    private ISysRoleService sysRoleService;
    @Resource
    private ISysDeptService sysDeptService;
    @Resource
    private ICsSysService csSysService;
    @Resource
    private StringRedisUtil redisUtil;

    /**
     * @param userId
     * @param isSearchCache
     * @author JPG
     * @since 2019/11/5 14:29
     */
    @Override
    public Admin getByUserId(Long userId, Boolean isSearchCache) {
        return adminDao.findByUserId(userId);
    }

    /**
     * 根据业务中心查询所有业务员
     *
     * @param storeId
     * @author JPG
     * @since 2019/11/5 14:43
     */
    @Override
    public List<Admin> getListByStoreId(Long storeId) {
        return null;
    }

    @Override
    public CacheAdminVo getCacheData(Long userId, Long roleId) {
        CacheAdminVo cacheAdminVo = new CacheAdminVo();
        Admin admin = adminDao.findByUserId(userId);
        if (admin == null) {
            return null;
        }
        BeanUtils.copyProperties(admin, cacheAdminVo);
        //查询角色信息
        ResultData<SelectRoleResp> resultData = sysRoleService.getById(roleId);
        if (resultData == null
                || resultData.getData() == null
                || resultData.getData().getRoleId() == null
                || resultData.getData().getDeptId() == null) {
            return null;
        }
        //查询部门信息
        ResultData<SelectDeptResp> resultDeptData = sysDeptService.getById(resultData.getData().getDeptId());
        if (resultDeptData == null
                || resultDeptData.getData() == null) {
            return null;
        }

        //根据部门ID查询业务中心ID
        List<Long> storeIds = csSysService.getStoreIdsByDeptId(resultData.getData().getDeptId());

        //TODO 缓存业务范围
        String key = RedisKeys.getUserBizScopeKey(ClientEnum.WEB_SERVER, userId);
        //redisUtil.
        return null;
    }
}
