package com.scau.village.module.resident.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.module.resident.entity.ResidentProfile;
import com.scau.village.module.resident.mapper.ResidentProfileMapper;
import com.scau.village.module.resident.service.ResidentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 居民档案服务实现类
 *
 * @author system
 * @since 2026-07-31
 */
@Slf4j
@Service
public class ResidentProfileServiceImpl
        extends ServiceImpl<ResidentProfileMapper, ResidentProfile>
        implements ResidentProfileService {

    @Override
    public List<ResidentProfile> searchByKeyword(Integer tenantId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.debug("搜索关键词为空，返回空列表");
            return List.of();
        }
        return baseMapper.searchByKeyword(tenantId, keyword.trim());
    }

    @Override
    public ResidentProfile matchByPhoneAndOwner(Integer tenantId, String phone, String ownerName) {
        if (phone == null || phone.isEmpty() || ownerName == null || ownerName.isEmpty()) {
            log.warn("匹配参数不完整：phone={}, ownerName={}", phone, ownerName);
            return null;
        }
        return baseMapper.matchByPhoneAndOwner(tenantId, phone, ownerName);
    }
}