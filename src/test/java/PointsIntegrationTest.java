import com.scau.village.VillageApplication;
import com.scau.village.common.utils.JwtUtils;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = VillageApplication.class)
@Transactional  // 测试后回滚
public class PointsIntegrationTest {

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
    private Integer testRuleId;
    private Integer testProductId;
    private Integer tenantId = 1;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        User user = new User();
        user.setTenantId(tenantId);
        user.setPhone("13900000000");
        user.setRealName("测试员");
        user.setRole("VILLAGER");
        user.setPoints(100);
        userService.save(user);
        testUserId = user.getId().longValue();

        // 创建测试规则
        PointsRule rule = new PointsRule();
        rule.setTenantId(tenantId);
        rule.setRuleName("测试规则");
        rule.setPoints(10);
        rule.setStatus(1);
        ruleService.save(rule);
        testRuleId = rule.getId();

        // 创建测试商品
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setName("测试商品");
        product.setPointsNeeded(50);
        product.setStock(5);
        product.setStatus(1);
        productService.save(product);
        testProductId = product.getId();
    }

    @Test
    void testFullFlow() {
        // 1. 提交积分申报
        ApplyDto dto = new ApplyDto();
        dto.setRuleId(testRuleId);
        dto.setDescription("参加打扫卫生");
        applyService.submitApply(dto, testUserId, tenantId);

        // 查询申报记录
        PointsApply apply = applyService.lambdaQuery()
                .eq(PointsApply::getUserId, testUserId)
                .one();
        Assertions.assertNotNull(apply);
        Assertions.assertEquals("pending", apply.getStatus());

        // 2. 审核通过
        applyService.approve(apply.getId(), 1L, true, "通过");

        // 验证积分增加
        User user = userService.getById(testUserId);
        Assertions.assertEquals(110, user.getPoints());

        // 3. 兑换商品
        ExchangeRecord record = exchangeService.exchange(testUserId, testProductId, tenantId);
        Assertions.assertNotNull(record);
        Assertions.assertEquals("pending", record.getStatus());
        // 检查积分扣减
        user = userService.getById(testUserId);
        Assertions.assertEquals(60, user.getPoints()); // 110 - 50 = 60

        // 4. 核销（需要传入管理员ID和核销方式）
        Integer adminUserId = 51; // 使用系统中已存在的管理员ID
        exchangeService.verifyCode(record.getExchangeCode(), tenantId, adminUserId, "manual");
        ExchangeRecord verified = exchangeService.getExchangeById(record.getId());
        Assertions.assertEquals("used", verified.getStatus());
    }
}