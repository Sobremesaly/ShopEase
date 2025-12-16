package com.shopease.vo;

import lombok.Data;
import java.util.List;

/**
 * 商品分类VO（树形结构）
 * @author 17813
 */
@Data
public class GoodsCategoryVO {
    /** 分类ID */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类ID（0表示一级分类） */
    private Long parentId;

    /** 排序 */
    private Integer sort;

    /** 子分类列表 */
    private List<GoodsCategoryVO> children;
}