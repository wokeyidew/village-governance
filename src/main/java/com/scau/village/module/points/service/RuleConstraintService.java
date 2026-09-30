package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.RuleConstraint;

import java.time.LocalDateTime;

/**
 * 规则约束服务。
 */
public interface RuleConstraintService extends IService<RuleConstraint> {

    /**
     * 查询指定时间生效的规则约束。
     *
     * @param ruleId 规则 ID
     * @param now    当前时间
     * @return 生效约束，不存在时返回 null
     */
    RuleConstraint getEffective(int ruleId, LocalDateTime now);

    /**
     * 根据约束计算时间窗口。
     *
     * @param constraint 规则约束
     * @param now        窗口结束时间
     * @return 计算后的时间窗口
     */
    Window resolve(RuleConstraint constraint, LocalDateTime now);

    /** 统计用户在时间窗口内已通过的指定规则申请数。 */
    int countInWindow(int userId, int ruleId, Window window);

    /**
     * 时间窗口值对象。
     */
    class Window {
        private final LocalDateTime start;
        private final LocalDateTime end;

        public Window(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }
    }
}
