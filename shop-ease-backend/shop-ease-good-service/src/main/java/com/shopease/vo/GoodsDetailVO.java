package com.shopease.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情VO
 */
@Data
public class GoodsDetailVO {
    /**
     * 商品ID
     */
    private Long id;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

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
     * 商品库存
     */
    private Integer stock;

    /**
     * 商品图片列表（拆分逗号分隔的字符串为列表）
     */
    private List<String> imageList;

    /**
     * 商品详情描述
     */
    private String description;
}