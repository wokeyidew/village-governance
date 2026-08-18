package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("points_flow")
public class PointsFlow {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer changeAmount;
    private String sourceType;
    private Integer sourceId;
    private String remark;
    private LocalDateTime createTime;
}