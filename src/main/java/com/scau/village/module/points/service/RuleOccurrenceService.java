package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.RuleOccurrence;

import java.util.List;

/** 规则族发生记录服务。 */
public interface RuleOccurrenceService extends IService<RuleOccurrence> {

    /** 查询用户在规则族中的下一次有效发生序号。 */
    int nextOccurrenceNo(int userId, String familyCode);

    /** 记录一次规则族发生。 */
    RuleOccurrence record(int userId, String familyCode, int occurrenceNo,
                          String applyId, String ruleVersion);

    /** 查询用户在规则族中的全部发生记录。 */
    List<RuleOccurrence> listByUser(int userId, String familyCode);
}
