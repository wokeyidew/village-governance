package com.scau.village.module.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.module.shop.entity.Product;
import com.scau.village.module.shop.mapper.ProductMapper;
import com.scau.village.module.shop.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 Service 实现
 *
 * @author system
 * @since 2026-07-17
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public List<Product> getProductList(Integer tenantId) {
        return lambdaQuery()
                .eq(Product::getTenantId, tenantId)
                .eq(Product::getStatus, 1) // 只返回上架商品
                .orderByDesc(Product::getCreateTime) // 按创建时间降序
                .list();
    }

    @Override
    public Page<Product> getProductPage(Integer tenantId, Integer pageNum, Integer pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        return lambdaQuery()
                .eq(Product::getTenantId, tenantId)
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime)
                .page(page);
    }

    @Override
    @Transactional
    public boolean updateProduct(Product product) {
        // 校验是否存在
        Product existing = getById(product.getId());
        if (existing == null) {
            return false;
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
        // status 通过上下架接口单独修改，不在此处理
        // 更新时间由 MyBatis-Plus 自动填充（如果配置了 FieldFill.INSERT_UPDATE）
        return updateById(existing);
    }
}