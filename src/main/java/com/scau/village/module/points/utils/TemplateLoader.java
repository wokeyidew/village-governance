package com.scau.village.module.points.utils;

import com.scau.village.module.points.entity.PointsRule;
import java.util.ArrayList;
import java.util.List;

public class TemplateLoader {

    public static List<PointsRule> getTemplatesByCode(String templateCode) {
        List<PointsRule> rules = new ArrayList<>();
        switch (templateCode) {
            case "party_building":
                rules.add(createRule("参加主题党日活动", "党建", 10, "single", 0, 1, 1));
                rules.add(createRule("学习强国积分", "党建", 5, "single", 1, 3, 2));
                rules.add(createRule("党员联系户", "党建", 20, "double", 1, 1, 3));
                break;
            case "environment":
                rules.add(createRule("门前三包达标", "环境", 5, "single", 1, 7, 1));
                rules.add(createRule("参与垃圾分类", "环境", 10, "single", 1, 1, 2));
                rules.add(createRule("美丽庭院评比", "环境", 30, "double", 1, 1, 3));
                break;
            case "volunteer":
                rules.add(createRule("村道清扫", "志愿", 15, "single", 1, 2, 1));
                rules.add(createRule("关爱老人", "志愿", 25, "double", 1, 1, 2));
                rules.add(createRule("协助村委工作", "志愿", 20, "single", 1, 1, 3));
                break;
            default:
                throw new IllegalArgumentException("未知模板代码: " + templateCode);
        }
        return rules;
    }

    private static PointsRule createRule(String name, String category, int points,
                                         String auditFlow, int needPhoto, int maxTimesPerDay, int sortOrder) {
        PointsRule rule = new PointsRule();
        rule.setRuleName(name);
        rule.setCategory(category);
        rule.setPoints(points);
        rule.setAuditFlow(auditFlow);
        rule.setNeedPhoto(needPhoto);
        rule.setMaxTimesPerDay(maxTimesPerDay);
        rule.setSortOrder(sortOrder);
        rule.setStatus(1);
        return rule;
    }
}