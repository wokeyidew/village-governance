package com.scau.village.module.points;

import com.scau.village.VillageApplication;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.points.dto.ApplyDto;
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.service.PointsApplyService;
import com.scau.village.module.points.service.PointsRuleService;
import com.scau.village.module.shop.entity.ExchangeRecord;
import com.scau.village.module.shop.entity.Product;
import com.scau.village.module.shop.service.ExchangeService;
import com.scau.village.module.shop.service.ProductService;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = VillageApplication.class)
@Transactional
public class PointsServiceCompleteTest {

    @Autowired
    private PointsRuleService ruleService;
    @Autowired
    private PointsApplyService applyService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ExchangeService exchangeService;

    private Long testUserId;
    private final Integer testTenantId = 1;
    private Integer testRuleId;
    private Integer testProductId;

    @BeforeEach
    public void setup() {
        // 模拟登录上下文
        UserContext ctx = new UserContext();
        ctx.setUserId(1L);
        ctx.setRole("VILLAGE_ADMIN");
        ctx.setTenantId(testTenantId);
        UserContext.set(ctx);

        // 创建测试用户
        User user = new User();
        user.setTenantId(testTenantId);
        user.setPhone("13800000000");
        user.setRealName("测试用户");
        user.setRole("VILLAGER");
        user.setPoints(1000);
        user.setPassword("$2a$10$dummy");
        userService.save(user);
        testUserId = user.getId().longValue();

        // 创建测试积分规则（每日上限2次）
        PointsRule rule = new PointsRule();
        rule.setTenantId(testTenantId);
        rule.setRuleName("单元测试规则");
        rule.setCategory("测试");
        rule.setPoints(10);
        rule.setAuditFlow("single");
        rule.setNeedPhoto(0);
        rule.setMaxTimesPerDay(2);
        rule.setStatus(1);
        rule.setSortOrder(1);
        ruleService.save(rule);
        testRuleId = rule.getId();

        // 创建测试商品
        Product product = new Product();
        product.setTenantId(testTenantId);
        product.setName("测试商品");
        product.setPointsNeeded(50);
        product.setStock(3);
        product.setStatus(1);
        product.setVersion(0);
        productService.save(product);
        testProductId = product.getId();
    }

    @Test
    public void testSubmitApply_Success() {
        ApplyDto dto = new ApplyDto();
        dto.setRuleId(testRuleId);
        dto.setDescription("参加测试活动");
        applyService.submitApply(dto, testUserId, testTenantId);
        PointsApply apply = applyService.lambdaQuery()
                .eq(PointsApply::getUserId, testUserId)
                .eq(PointsApply::getRuleId, testRuleId)
                .one();
        assertThat(apply).isNotNull();
        assertThat(apply.getStatus()).isEqualTo("pending");
    }

    @Test
    public void testSubmitApply_ExceedDailyLimit() {
        ApplyDto dto = new ApplyDto();
        dto.setRuleId(testRuleId);
        dto.setDescription("测试申报");

        // 第一次申报并审核
        applyService.submitApply(dto, testUserId, testTenantId);
        PointsApply apply1 = applyService.lambdaQuery()
                .eq(PointsApply::getUserId, testUserId)
                .eq(PointsApply::getRuleId, testRuleId)
                .eq(PointsApply::getStatus, "pending")
                .one();
        applyService.approve(apply1.getId(), 1L, true, "通过");

        // 第二次申报并审核
        applyService.submitApply(dto, testUserId, testTenantId);
        PointsApply apply2 = applyService.lambdaQuery()
                .eq(PointsApply::getUserId, testUserId)
                .eq(PointsApply::getRuleId, testRuleId)
                .eq(PointsApply::getStatus, "pending")
                .one();
        applyService.approve(apply2.getId(), 1L, true, "通过");

        // 第三次申报应触发异常
        assertThrows(BusinessException.class, () -> {
            applyService.submitApply(dto, testUserId, testTenantId);
        });
    }

    @Test
    public void testApprove_AddPoints() {
        ApplyDto dto = new ApplyDto();
        dto.setRuleId(testRuleId);
        dto.setDescription("增加积分测试");
        applyService.submitApply(dto, testUserId, testTenantId);
        PointsApply apply = applyService.lambdaQuery()
                .eq(PointsApply::getUserId, testUserId)
                .eq(PointsApply::getRuleId, testRuleId)
                .one();

        int beforePoints = userService.getById(testUserId).getPoints();
        applyService.approve(apply.getId(), 1L, true, "测试通过");
        int afterPoints = userService.getById(testUserId).getPoints();
        assertThat(afterPoints).isEqualTo(beforePoints + 10);
        assertThat(applyService.getById(apply.getId()).getStatus()).isEqualTo("approved");
    }

    @Test
    public void testExchange_Success() {
        ExchangeRecord record = exchangeService.exchange(testUserId, testProductId, testTenantId);
        assertThat(record).isNotNull();
        assertThat(record.getExchangeCode()).isNotNull();
        Product product = productService.getById(testProductId);
        assertThat(product.getStock()).isEqualTo(2);
        User user = userService.getById(testUserId);
        assertThat(user.getPoints()).isEqualTo(950);
    }

    @Test
    public void testExchange_InsufficientPoints() {
        User user = userService.getById(testUserId);
        user.setPoints(30);
        userService.updateById(user);
        assertThrows(BusinessException.class, () -> {
            exchangeService.exchange(testUserId, testProductId, testTenantId);
        });
    }

    @Test
    public void testExchange_OutOfStock() {
        Product product = productService.getById(testProductId);
        product.setStock(0);
        productService.updateById(product);
        assertThrows(BusinessException.class, () -> {
            exchangeService.exchange(testUserId, testProductId, testTenantId);
        });
    }
}