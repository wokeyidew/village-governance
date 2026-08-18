package com.scau.village.module.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.activity.dto.ActivitySignStatsDTO;
import com.scau.village.module.activity.dto.RegistrationDetailVO;
import com.scau.village.module.activity.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 活动报名 Mapper 接口
 * @author system
 * @since 2026-07-17
 */
@Mapper
public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {

    /**
     * 查询用户的所有报名记录（包含活动信息）
     * 如需返回包含活动名称等字段的VO，可使用此方法
     * 当前 Service 层使用 lambdaQuery 实现，此方法为扩展预留
     * 
     * @param userId 用户ID
     * @return 报名记录列表
     */
    List<ActivityRegistration> selectByUserId(@Param("userId") Integer userId);

    // ==================== 管理员签到统计（新增） ====================

    /**
     * 统计某个活动的签到签退数据
     * 
     * @param activityId 活动ID
     * @param tenantId   租户ID
     * @return 统计数据对象（报名人数、已签到、已签退等）
     */
    ActivitySignStatsDTO selectSignStatsByActivityId(@Param("activityId") Integer activityId,
                                                      @Param("tenantId") Integer tenantId);

    /**
     * 查询某个活动的所有报名明细（用于管理员查看谁签到/签退了）
     * 
     * @param activityId 活动ID
     * @param tenantId   租户ID
     * @return 报名明细列表（含用户姓名、签到状态、签退状态、时间等）
     */
    List<RegistrationDetailVO> selectRegistrationDetailsByActivityId(@Param("activityId") Integer activityId,
                                                                      @Param("tenantId") Integer tenantId);
}