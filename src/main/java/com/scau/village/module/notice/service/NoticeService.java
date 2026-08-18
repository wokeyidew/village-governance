package com.scau.village.module.notice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.notice.entity.Notice;

public interface NoticeService extends IService<Notice> {

    /**
     * 获取通知详情（自动增加阅读次数）
     * @param id 通知ID
     * @return 通知对象
     */
    Notice getNoticeDetail(Integer id);
}