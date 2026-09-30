package com.scau.village.module.points.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 提交评分请求DTO
 * 用于管理员在检查评分时，为某户提交一条或多条规则评分
 *
 * @author system
 * @since 2026-07-16
 */
@Data
public class ScoreSubmitDto {

    /**
     * 检查批次ID（必填，关联inspection_batch表）
     * 改为 String 类型，解决前端传递 19 位雪花 ID 时精度丢失的问题
     */
    @NotBlank(message = "检查批次ID不能为空")
    private String batchId;

    /**
     * 户主用户ID（必填，被评分的用户）
     */
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    /**
     * 选中的规则ID列表（必填，至少选择一条规则）
     * 每个规则自带分值（正数为加分，负数为扣分）
     */
    @NotEmpty(message = "至少选择一条评分规则")
    private List<Integer> rules;

    /**
     * 评分备注（可选）
     */
    private String description;

    /**
     * 评分图片列表（可选）
     * 扣分项强制要求上传照片，加分项可选
     * 前端以 multipart/form-data 或 base64 形式上传多张图片
     */
    private List<String> images;

    /**
     * 规则子项结果，仅规则 #16 门前三包使用；键为 component_code，值为是否通过。
     */
    private Map<String, Boolean> componentResults;

    /**
     * 容错处理：前端可能传空字符串 "" 或 [""]，转为空列表
     * 避免 Spring 无法将空字符串解析为 List<String> 导致 500
     */
    public void setImages(List<String> images) {
        if (images != null && images.size() == 1 && StringUtils.isBlank(images.get(0))) {
            this.images = new ArrayList<>();
        } else {
            this.images = images;
        }
    }

    /**
     * 检查当前选择的规则中是否包含扣分项（points < 0）
     * 注意：此方法需要配合 PointsRuleService 使用，Service 层应调用对应方法判断
     * 
     * @return true-包含扣分规则，false-仅加分规则
     */
    public boolean hasPenaltyRules() {
        // 此方法仅用于标识，实际判断由 Service 层通过规则ID查询 points 值完成
        // 保留此方法便于前端理解业务逻辑，但实际校验在 Service 层实现
        return false;
    }
}
