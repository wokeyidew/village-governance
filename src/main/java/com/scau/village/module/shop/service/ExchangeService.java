package com.scau.village.module.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.module.shop.dto.VerifyResultVO;
import com.scau.village.module.shop.entity.ExchangeRecord;

/**
 * 兑换服务接口
 *
 * @author system
 * @since 2026-07-18
 */
public interface ExchangeService {

    /**
     * 兑换商品（用户端）
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