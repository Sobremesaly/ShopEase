package com.shopease.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品入参DTO（接收前端新增/修改商品的参数）
 * 用@Data简化，自动生成Getter/Setter/toString等
 */
@Data
public class GoodsDTO {
    // 分类ID（对应Goods的categoryId）
    private Long categoryId;
    // 商品名称（对应Goods的name）
    private String name;
    // 商品副标题（对应Goods的subTitle）
    private String subTitle;
    // 商品价格（对应Goods的price）
    private BigDecimal price;
    // 商品库存（对应Goods的stock）
    private Integer stock;
    // 商品主图（对应Goods的image）
    private String image;
    // 商品详情描述（对应Goods的description）
    private String description;
    // 状态（1：上架，0：下架，对应Goods的status）
    private Integer status;
    // 排序（对应Goods的sort）
    private Integer sort;
    // 商品卖点（自定义字段，用于AI生成描述）
    private String sellingPoint;
    // 分类名称（前端可传入，用于AI生成，非数据库字段）
    private String categoryName;
}