package com.scau.village.module.policy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.policy.entity.Policy;

/**
 * 政策推送服务接口
 * @author system
 * @since 2026-07-17
 */
public interface PolicyService extends IService<Policy> {

    /**
     * 获取政策详情
     * @param id 政策ID
     * @return 政策对象
     */
    Policy getPolicyDetail(Integer id);
}