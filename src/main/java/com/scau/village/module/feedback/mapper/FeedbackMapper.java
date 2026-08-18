package com.scau.village.module.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.feedback.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 意见反馈 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 操作
 *
 * @author system
 * @since 2026-07-23
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    // 基础 CRUD 由 MyBatis-Plus 提供，无需额外编写
    // 如需复杂查询（如联表查询用户姓名），可在 Service 层通过 MyBatis-Plus 的 LambdaQueryWrapper 实现
    // 或在 XML 中编写自定义 SQL
}