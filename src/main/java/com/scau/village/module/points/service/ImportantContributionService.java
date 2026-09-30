package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.ImportantContribution;

import java.util.List;

/** 重要贡献认定服务。 */
public interface ImportantContributionService extends IService<ImportantContribution> {

    /** 提交一条待认定的重要贡献记录。 */
    ImportantContribution submit(int userId, int ruleId, String contributionDesc,
                                 String evidencePhotos, String sourceRef);

    /** 校验指定贡献记录已经针对用户和规则审批通过。 */
    ImportantContribution requireApproved(int userId, int ruleId, String contributionId);

    /** 审批通过一条重要贡献记录。 */
    void approve(String contributionId, int approverId, String remark);

    /** 查询租户下待审批的重要贡献。 */
    List<ImportantContribution> listPending(int tenantId);
}
