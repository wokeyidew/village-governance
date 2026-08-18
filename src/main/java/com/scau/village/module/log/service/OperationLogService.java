package com.scau.village.module.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.log.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {
    void log(Long userId, String operationType, String content);
}