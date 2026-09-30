package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.points.entity.RuleConstraint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 规则约束 Mapper。
 */
@Mapper
public interface RuleConstraintMapper extends BaseMapper<RuleConstraint> {

    /** 统计窗口内已通过的积分申请；租户条件由多租户拦截器追加。 */
    @Select("SELECT COUNT(*) FROM points_apply "
            + "WHERE user_id = #{userId} AND rule_id = #{ruleId} "
            + "AND status = 'approved' "
            + "AND create_time >= #{start} AND create_time <= #{end}")
    int countApprovedInWindow(@Param("userId") int userId,
                              @Param("ruleId") int ruleId,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);
}
