package com.scau.village.module.shop.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExchangeRecordVO {
    private Integer id;
    private Integer productId;
    private String productName;      // 商品名称
    private Integer pointsSpent;     // 消耗积分
    private String exchangeCode;
    private String status;           // pending, used, expired
    private LocalDateTime usedTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}