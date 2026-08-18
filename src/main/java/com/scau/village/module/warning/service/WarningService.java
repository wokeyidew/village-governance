package com.scau.village.module.warning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.warning.entity.WarningLog;

public interface WarningService extends IService<WarningLog> {
    void generateWarning(Integer tenantId, String type, String content);
}