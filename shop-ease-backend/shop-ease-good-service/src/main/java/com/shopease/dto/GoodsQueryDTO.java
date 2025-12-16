package com.shopease.dto;

import lombok.Data;

/**
 * 商品查询条件DTO
 * @author 17813
 */
@Data
public class GoodsQueryDTO {
    /** 页码（默认1） */
    private Long current = 1L;

    /** 页大小（默认10） */
    private Long size = 10L;

    /** 分类ID（筛选条件） */
    private Long categoryId;

    /** 关键词（商品名称/副标题搜索） */
    private String keyword;

    /** 商品状态（1：上架，0：下架，null：全部） */
    private Integer status;
}