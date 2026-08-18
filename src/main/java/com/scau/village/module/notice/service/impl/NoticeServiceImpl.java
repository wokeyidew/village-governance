package com.scau.village.module.notice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.notice.entity.Notice;
import com.scau.village.module.notice.mapper.NoticeMapper;
import com.scau.village.module.notice.service.NoticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    @Transactional
    public Notice getNoticeDetail(Integer id) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }
        // 阅读次数 +1（若实体中无 readCount 字段，可注释掉此行）
        notice.setReadCount(notice.getReadCount() + 1);
        updateById(notice);
        return notice;
    }
}