package com.scau.village.module.moment.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态评论展示VO
 *
 * @author system
 * @since 2026-07-17
 */
@Data
public class CommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论用户ID
     */
    private Integer userId;

    /**
     * 评论用户姓名
     */
    private String userName;

    /**
     * 评论用户头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论时间
     */
    private LocalDateTime createTime;
}