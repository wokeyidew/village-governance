package com.scau.village.module.policy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.policy.entity.Policy;
import com.scau.village.module.policy.mapper.PolicyMapper;
import com.scau.village.module.policy.service.PolicyService;
import org.springframework.stereotype.Service;

/**
 * 政策推送服务实现类
 * @author system
 * @since 2026-07-17
 */
@Service
public class PolicyServiceImpl extends ServiceImpl<PolicyMapper, Policy> implements PolicyService {

    @Override
    public Policy getPolicyDetail(Integer id) {
        Policy policy = getById(id);
        if (policy == null) {
            throw new BusinessException("政策不存在");
        }
        return policy;
    }
}