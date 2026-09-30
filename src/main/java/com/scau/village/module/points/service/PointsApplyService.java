package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.dto.ApplyDto;
import com.scau.village.module.points.dto.ScoreSubmitDto;
import com.scau.village.module.points.entity.PointsApply;

/**
 * 积分申请/评分记录服务接口
 * 涵盖村民申报审核、管理员现场评分两大业务场景
 *
 * @author system
 * @since 2026-07-16
 */
public interface PointsApplyService extends IService<PointsApply> {

    /**
     * 村民申报积分
     * 村民自主申报积分，状态为待审核，需管理员审核后方可生效
     *
     * @param dto      申报信息
     * @param userId   申报人ID
     * @param tenantId 租户ID
     */
    void submitApply(ApplyDto dto, Long userId, Integer tenantId);

    /**
     * 审核积分申报（村民自主申报）
     * 管理员审核通过或驳回村民的积分申报
     *
     * @param applyId   积分申报记录ID
     * @param auditorId 审核人ID（管理员）
     * @param approved  是否通过：true-通过，false-驳回
     * @param remark    审核备注（驳回时建议填写原因）
     */
    void approve(String applyId, Long auditorId, Boolean approved, String remark);

    /**
     * 管理员现场评分
     * 管理员入户检查时直接评分，积分立即生效，无需审核。
     * 扣分项会强制绑定证据（照片），加分项可选。
     *
     * @param dto         评分提交参数（包含批次、用户、规则、备注、图片等）
     * @param inspectorId 检查人ID（当前登录管理员）
     * @param tenantId    租户ID
     */
    void saveAdminScore(ScoreSubmitDto dto, Integer inspectorId, Integer tenantId);

    /** 双审全部通过后完成积分、流水、证据和整改结算。 */
    void postDoubleApproval(String applyId, int reviewerId);
}
