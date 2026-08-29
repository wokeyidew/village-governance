package com.scau.village.module.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginVO {
    private Boolean registered;
    private String token;
    private String openid;
}