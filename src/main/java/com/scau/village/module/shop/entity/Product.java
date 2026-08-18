package com.scau.village.module.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer tenantId;
    private String name;
    private Integer pointsNeeded;
    private Integer stock;
    private String imageUrl;
    private String description;
    private Integer status;
    @Version
    private Integer version;
    private LocalDateTime createTime;
}