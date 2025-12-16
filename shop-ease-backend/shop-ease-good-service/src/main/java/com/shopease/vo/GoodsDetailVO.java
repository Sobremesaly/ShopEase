package com.shopease.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品详情VO
 */
@Data
public class GoodsDetailVO {
    /** 商品ID */
    private Long id;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 商品名称 */
    private String name;

    /** 商品副标题 */
    private String subTitle;

    /** 商品价格 */
    private BigDecimal price;

    /** 商品库存 */
    private Integer stock;

    /** 商品主图（多个图片用逗号分隔） */
    private String image;

    /** 商品详情描述 */
    private String description;

    /** 状态（1：上架，0：下架） */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}