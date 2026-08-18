package com.scau.village.module.points.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class ApplyDto {
    @NotNull
    private Integer ruleId;
    private String description;
    private String images;
}