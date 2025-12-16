package com.shopease.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品列表VO
 * @author 17813
 */
@Data
public class GoodsListVO {
    /** 商品ID */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品副标题 */
    private String subTitle;

    /** 商品价格 */
    private BigDecimal price;

    /** 商品库存 */
    private Integer stock;

    /** 商品主图 */
    private String image;

    /** 商品状态（1：上架，0：下架） */
    private Integer status;

    /** 分类名称 */
    private String categoryName;

    /** 排序 */
    private Integer sort;
}