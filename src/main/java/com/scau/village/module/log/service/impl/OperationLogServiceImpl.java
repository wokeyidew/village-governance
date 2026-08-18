package com.scau.village.module.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.module.log.entity.OperationLog;
import com.scau.village.module.log.mapper.OperationLogMapper;
import com.scau.village.module.log.service.OperationLogService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
    @Override
    public void log(Long userId, String operationType, String content) {
        OperationLog log = new OperationLog();
        log.setUserId(userId.intValue());
        log.setOperationType(operationType);
        log.setContent(content);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }
}