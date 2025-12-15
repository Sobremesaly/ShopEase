package com.shopease.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品列表展示VO
 */
@Data
public class GoodsListVO {
    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品副标题
     */
    private String subTitle;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品主图（取第一个图片）
     */
    private String image;

    /**
     * 分类名称（可选，前端展示用）
     */
    private String categoryName;
}