package com.scau.village.module.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.shop.entity.Product;

import java.util.List;

/**
 * 商品 Service 接口
 *
 * @author system
 * @since 2026-07-17
 */
public interface ProductService extends IService<Product> {

    /**
     * 获取商品列表（按创建时间降序）
     * 最新上传的排在前面
     *
     * @param tenantId 租户ID
     * @return 商品列表
     */
    List<Product> getProductList(Integer tenantId);

    /**
     * 分页获取商品列表（按创建时间降序）
     * 最新上传的排在前面
     *
     * @param tenantId 租户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页对象
     */
    Page<Product> getProductPage(Integer tenantId, Integer pageNum, Integer pageSize);

    /**
     * 更新商品信息
     * 只允许更新：名称、所需积分、库存、图片、描述
     *
     * @param product 商品对象（必须包含id）
     * @return 是否更新成功
     */
    boolean updateProduct(Product product);
}