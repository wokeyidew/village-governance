package com.scau.village.module.points.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointsFlowVO {
    private Integer id;
    private Integer userId;
    private Integer changeAmount;
    private String sourceType;
    private String sourceTypeText;  // 中文类型
    private Integer sourceId;
    private String remark;
    private String images;          // 关联的图片 URL
    private String description;     // 关联的描述
    private LocalDateTime createTime;
}