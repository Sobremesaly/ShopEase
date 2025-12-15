package com.shopease.dto;

import lombok.Data;

/**
 * 商品查询入参
 * 包含分页、分类、关键词等参数
 */
@Data
public class GoodsQueryDTO {
    /**
     * 页码（默认1）
     */
    private Integer pageNum = 1;

    /**
     * 每页条数（默认10）
     */
    private Integer pageSize = 10;

    /**
     * 分类ID（可选，用于筛选）
     */
    private Long categoryId;

    /**
     * 关键词（可选，用于搜索商品名称）
     */
    private String keyword;
}