package com.scau.village.module.warning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.module.warning.entity.WarningLog;
import com.scau.village.module.warning.mapper.WarningLogMapper;
import com.scau.village.module.warning.service.WarningService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class WarningServiceImpl extends ServiceImpl<WarningLogMapper, WarningLog> implements WarningService {
    @Override
    public void generateWarning(Integer tenantId, String type, String content) {
        WarningLog log = new WarningLog();
        log.setTenantId(tenantId);
        log.setWarningType(type);
        log.setWarningContent(content);
        log.setOccurredAt(LocalDateTime.now());
        log.setIsResolved(0);
        save(log);
    }
}