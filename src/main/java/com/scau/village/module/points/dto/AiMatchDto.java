package com.scau.village.module.points.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * AI匹配请求DTO
 * 前端拍照后将图片传给后端，后端调用AI服务进行规则匹配
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class AiMatchDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Base64编码的图片数据
     * 前端拍照后转换为base64字符串，不含data:image前缀
     * 示例：iVBORw0KGgoAAAANS...
     */
    @NotBlank(message = "图片数据不能为空")
    private String imageBase64;
}