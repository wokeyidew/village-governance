package com.scau.village.module.points.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TemplateEnum {
    PARTY_BUILDING("party_building", "党建引领型"),
    ENVIRONMENT("environment", "人居环境型"),
    VOLUNTEER("volunteer", "志愿服务型");

    private final String code;
    private final String name;
}