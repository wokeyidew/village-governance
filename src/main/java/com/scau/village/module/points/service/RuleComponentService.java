package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.RuleComponent;
import com.scau.village.module.points.entity.RuleComponentResult;

import java.util.List;
import java.util.Map;

/** 规则子项定义与结果服务。 */
public interface RuleComponentService extends IService<RuleComponent> {

    /** 查询指定规则的全部子项定义。 */
    List<RuleComponent> listByRule(int ruleId);

    /** 根据子项通过状态计算规则实际得分，缺失子项按不通过处理。 */
    int calculateScore(int ruleId, Map<String, Boolean> componentResults);

    /** 保存一次申请中各子项的通过/不通过结果。 */
    List<RuleComponentResult> saveResults(String applyId, int ruleId,
                                          Map<String, Boolean> componentResults);
}
