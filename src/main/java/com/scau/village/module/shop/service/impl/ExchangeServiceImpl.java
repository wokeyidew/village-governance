package com.scau.village.module.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.common.utils.WechatOfficialUtil;
import com.scau.village.common.utils.WechatWorkUtil;
import com.scau.village.module.log.service.OperationLogService;
import com.scau.village.module.notification.entity.SubscribeMessage;
import com.scau.village.module.notification.service.SubscribeMessageService;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.mapper.PointsFlowMapper;
import com.scau.village.module.shop.dto.VerifyResultVO;
import com.scau.village.module.shop.entity.ExchangeRecord;
import com.scau.village.module.shop.entity.Product;
import com.scau.village.module.shop.mapper.ExchangeRecordMapper;
import com.scau.village.module.shop.mapper.ProductMapper;
import com.scau.village.module.shop.service.ExchangeService;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final ExchangeRecordMapper recordMapper;
    private final PointsFlowMapper pointsFlowMapper;
    private final OperationLogService operationLogService;
    private final SubscribeMessageService subscribeMessageService;
    private final WechatWorkUtil wechatWorkUtil;
    private final WechatOfficialUtil wechatOfficialUtil;

    @Value("${wechat.subscribe.template.exchange:}")
    private String exchangeTemplateId;

    @Value("${wechat.official.template.exchange:}")
    private String officialTemplateId;

    private static final int MAX_RETRY = 3;

    @Override
    @Transactional
    public ExchangeRecord exchange(Long userId, Integer productId, Integer tenantId) {
        for (int retryCount = 0; retryCount < MAX_RETRY; retryCount++) {
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStatus() != 1 || product.getStock() <= 0) {
                throw new BusinessException("商品不存在或库存不足");
            }

            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // ================================================================
            // 【修复点 1】积分校验：使用 availablePoints（可用积分）
            // 注意：不是 points（旧字段），更不是 totalEarnedPoints
            // ================================================================
            if (user.getAvailablePoints() == null) {
                user.setAvailablePoints(0);
            }
            if (user.getAvailablePoints() < product.getPointsNeeded()) {
                throw new BusinessException("可用积分不足，当前可用：" + user.getAvailablePoints() + "，需要：" + product.getPointsNeeded());
            }

            // ================================================================
            // 【修复点 2】积分扣减：同时更新 availablePoints 和 points（兼容）
            // 注意：totalEarnedPoints 保持不变（只增不减）
            // ================================================================
            int pointsNeeded = product.getPointsNeeded();
            user.setAvailablePoints(user.getAvailablePoints() - pointsNeeded);
            user.setPoints(user.getPoints() - pointsNeeded);  // 兼容旧字段
            // 注意：不修改 totalEarnedPoints

            int updateUserResult = userMapper.updateById(user);
            if (updateUserResult == 0) {
                log.warn("更新用户积分失败，userId={}, 重试次数={}", userId, retryCount);
                continue;
            }

            product.setStock(product.getStock() - 1);
            if (product.getStock() == 0) {
                product.setStatus(0);
            }
            int updateProductResult = productMapper.updateById(product);
            if (updateProductResult > 0) {
                String exchangeCode = generateUniqueCode();
                if (exchangeCode == null) {
                    throw new BusinessException("系统繁忙，请稍后重试");
                }

                ExchangeRecord record = new ExchangeRecord();
                record.setTenantId(tenantId);
                record.setUserId(userId.intValue());
                record.setProductId(productId);
                record.setExchangeCode(exchangeCode);
                record.setStatus("pending");
                record.setExpireTime(null);
                record.setCreateTime(LocalDateTime.now());
                recordMapper.insert(record);

                // ================================================================
                // 【修复点 3】记录积分流水：sourceId 转为 String
                // 注意：PointsFlow.sourceId 已改为 String 类型
                // ================================================================
                PointsFlow flow = new PointsFlow();
                flow.setUserId(userId.intValue());
                flow.setChangeAmount(-pointsNeeded);
                flow.setSourceType("exchange");
                flow.setSourceId(record.getId().toString());  // ✅ 转为 String
                flow.setRemark("兑换商品：" + product.getName());
                flow.setCreateTime(LocalDateTime.now());
                flow.setTenantId(tenantId);
                // batchId, batchName, applyId 保持 null（不适用于兑换场景）
                pointsFlowMapper.insert(flow);

                operationLogService.log(userId, "POINTS_EXCHANGE",
                        String.format("兑换商品 %s (ID:%d)，消耗积分 %d，核销码 %s",
                                product.getName(), productId, pointsNeeded, exchangeCode));

                sendNotifications(user, product, exchangeCode, tenantId);

                log.info("兑换成功，userId={}, productId={}, code={}, 可用积分剩余={}",
                        userId, productId, exchangeCode, user.getAvailablePoints());
                return record;
            } else if (retryCount == MAX_RETRY - 1) {
                log.error("兑换失败，乐观锁冲突超过最大重试次数，userId={}, productId={}", userId, productId);
                throw new BusinessException("兑换失败，请稍后重试");
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("兑换过程被中断");
            }
        }
        throw new BusinessException("兑换失败，请稍后重试");
    }

    // ================================================================
    // 【以下方法保持不变】
    // ================================================================

    private void sendNotifications(User user, Product product, String code, Integer tenantId) {
        try {
            if (exchangeTemplateId != null && !exchangeTemplateId.isEmpty()) {
                List<SubscribeMessage> subscribers = subscribeMessageService.getSubscribersByTemplateId(exchangeTemplateId);
                for (SubscribeMessage sub : subscribers) {
                    if (sub.getStatus() == 1) {
                        Map<String, Map<String, String>> data = new HashMap<>();
                        data.put("thing1", new HashMap<String, String>() {{
                            put("value", "积分兑换通知");
                        }});
                        data.put("thing2", new HashMap<String, String>() {{
                            put("value", user.getRealName() + " 兑换了 " + product.getName());
                        }});
                        data.put("thing3", new HashMap<String, String>() {{
                            put("value", "核销码: " + code);
                        }});
                        data.put("time4", new HashMap<String, String>() {{
                            put("value", LocalDateTime.now().toString());
                        }});
                        subscribeMessageService.sendSubscribeMessage(
                                sub.getOpenid(),
                                exchangeTemplateId,
                                "pages/admin/verify/verify",
                                data
                        );
                    }
                }
            }

            wechatWorkUtil.sendExchangeNotification(user.getRealName(), product.getName(), code);

            if (officialTemplateId != null && !officialTemplateId.isEmpty()) {
                // 可扩展：发送给特定管理员
            }

        } catch (Exception e) {
            log.error("发送兑换通知失败", e);
        }
    }

    private String generateUniqueCode() {
        String chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
        for (int i = 0; i < 100; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                sb.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            String code = sb.toString();
            LambdaQueryWrapper<ExchangeRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExchangeRecord::getExchangeCode, code);
            if (recordMapper.selectCount(wrapper) == 0) {
                return code;
            }
        }
        return null;
    }

    private ExchangeRecord doVerify(String code, Integer tenantId, Integer adminUserId, String verifyMethod) {
        ExchangeRecord record = recordMapper.selectOne(new LambdaQueryWrapper<ExchangeRecord>()
                .eq(ExchangeRecord::getExchangeCode, code)
                .eq(ExchangeRecord::getTenantId, tenantId));
        if (record == null) {
            throw new BusinessException("核销码无效");
        }
        if (!"pending".equals(record.getStatus())) {
            if ("used".equals(record.getStatus())) {
                throw new BusinessException("核销码已使用");
            }
            throw new BusinessException("核销码状态异常");
        }
        record.setStatus("used");
        record.setUsedTime(LocalDateTime.now());
        record.setVerifiedBy(adminUserId);
        record.setVerifyMethod(verifyMethod);
        recordMapper.updateById(record);

        operationLogService.log(adminUserId.longValue(), "EXCHANGE_VERIFY",
                String.format("核销兑换码 %s，商品ID %d，被兑换用户ID %d，方式 %s",
                        code, record.getProductId(), record.getUserId(), verifyMethod));

        log.info("核销成功，code={}, productId={}, operatorId={}, method={}",
                code, record.getProductId(), adminUserId, verifyMethod);
        return record;
    }

    @Override
    @Transactional
    public void verifyCode(String code, Integer tenantId, Integer adminUserId, String verifyMethod) {
        doVerify(code, tenantId, adminUserId, verifyMethod);
    }

    @Override
    @Transactional
    public VerifyResultVO verifyAndGetResult(String code, Integer tenantId, Integer adminUserId,
                                            String adminName, String verifyMethod) {
        ExchangeRecord record = doVerify(code, tenantId, adminUserId, verifyMethod);

        User user = userMapper.selectById(record.getUserId());
        Product product = productMapper.selectById(record.getProductId());

        VerifyResultVO vo = new VerifyResultVO();
        vo.setExchangeCode(record.getExchangeCode());
        vo.setStatus("success");
        vo.setMessage("核销成功");
        vo.setProductName(product != null ? product.getName() : "已下架商品");
        vo.setUserName(user != null ? user.getRealName() : "未知用户");
        vo.setExchangedAt(record.getCreateTime());
        vo.setVerifiedAt(record.getUsedTime());
        vo.setVerifyMethod(record.getVerifyMethod());
        vo.setVerifiedBy(adminUserId);
        vo.setVerifiedByName(adminName);
        return vo;
    }

    @Override
    public Page<ExchangeRecord> listPendingVerifies(Integer tenantId, Integer page, Integer size) {
        Page<ExchangeRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ExchangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeRecord::getTenantId, tenantId)
                .eq(ExchangeRecord::getStatus, "pending")
                .orderByDesc(ExchangeRecord::getCreateTime);
        return recordMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public ExchangeRecord getExchangeById(Integer id) {
        return recordMapper.selectById(id);
    }
}