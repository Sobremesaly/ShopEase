package com.shopease.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopease.entity.GoodsCategory;
import com.shopease.vo.GoodsCategoryVO;

import java.util.List;

/**
 * 商品分类Service接口
 */
public interface GoodsCategoryService extends IService<GoodsCategory> {
    /**
     * 查询分类树形结构（一级分类+二级分类）
     * @return 分类树形列表
     */
    List<GoodsCategoryVO> getCategoryTree();
}