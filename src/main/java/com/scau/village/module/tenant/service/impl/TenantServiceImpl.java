package com.scau.village.module.tenant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.module.tenant.entity.Tenant;
import com.scau.village.module.tenant.mapper.TenantMapper;
import com.scau.village.module.tenant.service.TenantService;
import org.springframework.stereotype.Service;

/**
 * 租户 Service 实现
 */
@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {
}