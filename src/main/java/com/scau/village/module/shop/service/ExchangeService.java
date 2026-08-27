package com.scau.village.module.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.module.shop.dto.VerifyResultVO;
import com.scau.village.module.shop.entity.ExchangeRecord;

/**
 * 兑换服务接口
 * 
 * 积分体系说明（v2.0）：
 * - total_earned_points（总获得积分）：历史累计获得的积分总和，只增不减，兑换时不扣减
 * - available_points（可用积分）：当前可使用的积分余额，获得时增加，兑换时扣减
 * 
 * 兑换规则：
 * - 兑换时检查 available_points 是否足够
 * - 兑换成功后扣减 available_points，不影响 total_earned_points
 * - 兑换消耗的积分记录在 exchange_record 表中
 *
 * @author system
 * @since 2026-07-18
 */
public interface ExchangeService {

    /**
     * 兑换商品（用户端）
     * 
     * 流程说明：
     * 1. 校验商品是否存在、库存是否充足
     * 2. 校验用户是否存在
     * 3. 校验用户可用积分（available_points）是否 >= 商品所需积分
     * 4. 扣减用户可用积分（available_points），不影响总获得积分（total_earned_points）
     * 5. 扣减商品库存
     * 6. 生成兑换记录和核销码
     * 7. 记录积分流水（source_type = 'exchange'）
     *
     * @param userId     用户ID
     * @param productId  商品ID
     * @param tenantId   租户ID
     * @return 兑换记录（包含生成的核销码）
     */
    ExchangeRecord exchange(Long userId, Integer productId, Integer tenantId);

    /**
     * 核销兑换码（管理员端）
     *
     * @param code         8位核销码
     * @param tenantId     租户ID（用于校验）
     * @param adminUserId  核销人ID（管理员）
     * @param verifyMethod 核销方式：scan-扫码，manual-手动输入
     */
    void verifyCode(String code, Integer tenantId, Integer adminUserId, String verifyMethod);

    /**
     * 核销兑换码并返回详细结果（管理员端）
     *
     * @param code         8位核销码
     * @param tenantId     租户ID（用于校验）
     * @param adminUserId  核销人ID（管理员）
     * @param adminName    核销人姓名（用于返回）
     * @param verifyMethod 核销方式：scan-扫码，manual-手动输入
     * @return 核销结果VO，包含商品名、用户名、核销时间等
     */
    VerifyResultVO verifyAndGetResult(String code, Integer tenantId, Integer adminUserId,
                                      String adminName, String verifyMethod);

    /**
     * 分页查询待核销的兑换记录（管理员端）
     *
     * @param tenantId 租户ID
     * @param page     页码
     * @param size     每页大小
     * @return 待核销记录分页
     */
    Page<ExchangeRecord> listPendingVerifies(Integer tenantId, Integer page, Integer size);

    /**
     * 根据ID获取兑换记录详情（含核销信息）
     *
     * @param id 兑换记录ID
     * @return 兑换记录
     */
    ExchangeRecord getExchangeById(Integer id);

}