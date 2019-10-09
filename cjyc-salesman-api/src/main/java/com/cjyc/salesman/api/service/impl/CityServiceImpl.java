package com.cjyc.salesman.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjkj.common.redis.template.StringRedisUtil;
import com.cjyc.common.model.dao.ICityDao;
import com.cjyc.common.model.entity.City;
import com.cjyc.common.model.keys.RedisKeys;
import com.cjyc.salesman.api.service.ICityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 韵车城市信息表 服务实现类
 * </p>
 *
 * @author JPG
 * @since 2019-09-30
 */
@Service
public class CityServiceImpl extends ServiceImpl<ICityDao, City> implements ICityService {


    @Autowired
    private StringRedisUtil redisUtil;
    
    @Resource
    private ICityDao cityDao;

    @Override
    public City findById(String cityCode) {

        return cityDao.findById(cityCode);
    }
}