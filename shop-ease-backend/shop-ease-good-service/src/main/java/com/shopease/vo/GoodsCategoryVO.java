package com.shopease.vo;

import lombok.Data;
import java.util.List;

/**
 * 商品分类VO（支持树形结构，比如一级分类下的二级分类）
 */
@Data
public class GoodsCategoryVO {
    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 子分类列表（二级分类）
     */
    private List<GoodsCategoryVO> children;
}