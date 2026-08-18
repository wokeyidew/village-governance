package com.scau.village.module.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.shop.dto.VerifyResultVO;
import com.scau.village.module.shop.entity.ExchangeRecord;
import com.scau.village.module.shop.entity.Product;
import com.scau.village.module.shop.mapper.ExchangeRecordMapper;
import com.scau.village.module.shop.service.ExchangeService;
import com.scau.village.module.shop.service.ProductService;
import com.scau.village.module.shop.vo.ExchangeRecordVO;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分超市控制器
 *
 * @author system
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final ExchangeService exchangeService;
    private final ExchangeRecordMapper exchangeRecordMapper;
    private final UserMapper userMapper; // ✅ 注入 UserMapper，用于查询管理员姓名

    /**
     * 商品列表（村民查看）- 按创建时间降序
     */
    @GetMapping("/products")
    public Result<?> listProducts() {
        UserContext ctx = UserContext.get();
        Integer tenantId = (ctx != null) ? ctx.getTenantId() : 1;
        List<Product> list = productService.lambdaQuery()
                .eq(Product::getTenantId, tenantId)
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime) // 按创建时间倒序
                .list();
        return Result.success(list);
    }

    /**
     * 新增商品（管理员）
     */
    @PostMapping("/products")
    public Result<Void> addProduct(@RequestBody Product product) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        product.setTenantId(ctx.getTenantId());
        product.setStatus(1);
        product.setCreateTime(java.time.LocalDateTime.now());
        productService.save(product);
        return Result.success(null);
    }

    /**
     * 编辑商品信息（管理员）
     * 只允许更新：名称、所需积分、库存、图片、描述
     */
    @PutMapping("/products/{id}")
    public Result<Void> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        Product existing = productService.getById(id);
        if (existing == null) {
            return Result.error(404, "商品不存在");
        }
        // 只更新允许修改的字段
        if (product.getName() != null) {
            existing.setName(product.getName());
        }
        if (product.getPointsNeeded() != null) {
            existing.setPointsNeeded(product.getPointsNeeded());
        }
        if (product.getStock() != null) {
            existing.setStock(product.getStock());
        }
        if (product.getImageUrl() != null) {
            existing.setImageUrl(product.getImageUrl());
        }
        if (product.getDescription() != null) {
            existing.setDescription(product.getDescription());
        }
        // status 不通过此接口修改，使用上下架接口
        // version 由数据库乐观锁自动处理，无需手动设置
        productService.updateById(existing);
        return Result.success(null);
    }

    /**
     * 兑换商品（村民）
     * 返回包含核销码的兑换记录
     */
    @PostMapping("/exchange/{productId}")
    public Result<ExchangeRecord> exchange(@PathVariable Integer productId) {
        SecurityUtils.checkRole("VILLAGER");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();
        Integer tenantId = ctx.getTenantId();
        ExchangeRecord record = exchangeService.exchange(userId, productId, tenantId);
        return Result.success(record);
    }

    /**
     * 核销兑换码（村委/网格员）- 仅核销，返回空
     * 支持扫码和手动输入，核销方式由前端传递
     */
    @PostMapping("/verify")
    public Result<Void> verify(@RequestParam String code,
                               @RequestParam(defaultValue = "manual") String verifyMethod) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer adminUserId = ctx.getUserId().intValue();
        Integer tenantId = ctx.getTenantId();
        exchangeService.verifyCode(code, tenantId, adminUserId, verifyMethod);
        return Result.success(null);
    }

    /**
     * 核销兑换码并返回详细结果（村委/网格员）
     * 适用于扫码枪/小程序核销后需要展示详情
     */
    @PostMapping("/verify/detail")
    public Result<VerifyResultVO> verifyDetail(@RequestParam String code,
                                               @RequestParam(defaultValue = "manual") String verifyMethod) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer adminUserId = ctx.getUserId().intValue();
        Integer tenantId = ctx.getTenantId();

        // ✅ 从数据库查询管理员姓名
        String adminName = "管理员";
        if (adminUserId != null) {
            User admin = userMapper.selectById(adminUserId);
            if (admin != null && admin.getRealName() != null && !admin.getRealName().isEmpty()) {
                adminName = admin.getRealName();
            }
        }

        VerifyResultVO result = exchangeService.verifyAndGetResult(code, tenantId, adminUserId, adminName, verifyMethod);
        return Result.success(result);
    }

    /**
     * 管理员查看待核销列表
     * 返回所有 status = 'pending' 的兑换记录
     */
    @GetMapping("/admin/pending-list")
    public Result<Page<ExchangeRecord>> listPendingVerifies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        Page<ExchangeRecord> result = exchangeService.listPendingVerifies(tenantId, page, size);
        return Result.success(result);
    }

    /**
     * 我的兑换记录（村民查看自己的兑换历史） - 原接口
     */
    @GetMapping("/exchange-records")
    public Result<Page<ExchangeRecordVO>> getExchangeRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size) {
        return getExchangeList(page, size);
    }

    /**
     * 我的兑换记录（村民查看自己的兑换历史） - 新增适配前端路径
     */
    @GetMapping("/exchange/list")
    public Result<Page<ExchangeRecordVO>> getExchangeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }

        Long userId = ctx.getUserId();
        Integer tenantId = ctx.getTenantId();

        Page<ExchangeRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ExchangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeRecord::getUserId, userId)
                .eq(ExchangeRecord::getTenantId, tenantId)
                .orderByDesc(ExchangeRecord::getCreateTime);

        Page<ExchangeRecord> recordPage = exchangeRecordMapper.selectPage(pageParam, wrapper);

        List<ExchangeRecordVO> voList = recordPage.getRecords().stream().map(record -> {
            ExchangeRecordVO vo = new ExchangeRecordVO();
            BeanUtils.copyProperties(record, vo);
            Product product = productService.getById(record.getProductId());
            vo.setProductName(product != null ? product.getName() : "已下架");
            vo.setPointsSpent(product != null ? product.getPointsNeeded() : 0);
            return vo;
        }).collect(Collectors.toList());

        Page<ExchangeRecordVO> voPage = new Page<>(pageParam.getCurrent(), pageParam.getSize(), recordPage.getTotal());
        voPage.setRecords(voList);
        return Result.success(voPage);
    }

    /**
     * 删除商品（管理员）
     */
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(@PathVariable Integer id) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        productService.removeById(id);
        return Result.success(null);
    }

    /**
     * 更新商品状态（上下架）
     */
    @PutMapping("/products/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Integer id, @RequestBody Product product) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        Product existing = productService.getById(id);
        if (existing == null) {
            return Result.error("商品不存在");
        }
        existing.setStatus(product.getStatus());
        productService.updateById(existing);
        return Result.success(null);
    }
}