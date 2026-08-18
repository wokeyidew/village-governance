package com.scau.village.module.moment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MomentVO {
    private Long id;
    private Integer userId;
    private String userName;
    private String userAvatar;
    private String content;
    private String images;
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean isLiked;
    private LocalDateTime createTime;
    private List<CommentVO> comments;
}