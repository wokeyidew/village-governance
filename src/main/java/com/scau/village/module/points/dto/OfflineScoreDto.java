package com.scau.village.module.points.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 离线评分同步请求DTO
 * 用于前端在离线模式下完成评分后，网络恢复时批量同步数据
 * 包含客户端唯一事件ID以实现幂等性
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class OfflineScoreDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户端事件ID（必填）
     * 由前端生成的唯一标识（UUID），用于服务端幂等校验，防止重复提交
     */
    @NotBlank(message = "客户端事件ID不能为空")
    private String clientEventId;

    /**
     * 检查批次ID（必填）
     */
    @NotNull(message = "检查批次ID不能为空")
    private Long batchId;

    /**
     * 被评分的户主用户ID（必填）
     */
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    /**
     * 选中的规则ID列表（必填，至少一条）
     */
    @NotEmpty(message = "至少选择一条评分规则")
    private List<Integer> rules;

    /**
     * 评分备注（可选）
     */
    private String description;

    /**
     * 评分图片列表（可选，扣分项强制要求）
     * 离线模式下，图片以base64编码字符串形式传输
     * 服务端接收到后解码保存
     */
    private List<String> images;

    /**
     * 离线评分时间（必填）
     * 前端在离线时记录的时间戳，用于审计追踪
     */
    @NotNull(message = "离线评分时间不能为空")
    private LocalDateTime offlineTime;

    /**
     * 拍摄位置（可选）
     * GPS坐标或地址描述
     */
    private String location;
}