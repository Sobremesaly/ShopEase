package com.shopease.dto;

import lombok.Data;

/**
 * AI商品DTO（用于传递AI生成描述/标题的参数）
 * 用@Data简化，自动生成Getter/Setter/toString等
 */
@Data
public class AiGoodsDTO {

    // 商品名称（对应Goods的name）
    private String goodsName;

    // 分类名称（AI生成需要分类名称，而非分类ID）
    private String categoryName;

    // 商品卖点（自定义字段，用于AI生成）
    private String sellingPoint;

    // 原始副标题（对应Goods的subTitle，用于AI优化）
    private String originalSubTitle;
}