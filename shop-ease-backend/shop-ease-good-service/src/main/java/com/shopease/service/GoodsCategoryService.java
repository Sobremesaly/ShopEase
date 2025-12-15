package com.shopease.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopease.entity.GoodsCategory;
import com.shopease.vo.GoodsCategoryVO;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface GoodsCategoryService extends IService<GoodsCategory> {
    /**
     * 查询分类树形结构（一级分类+二级分类）
     */
    List<GoodsCategoryVO> getCategoryTree();

    /**
     * 根据分类ID查询分类名称
     */
    String getCategoryNameById(Long categoryId);
}