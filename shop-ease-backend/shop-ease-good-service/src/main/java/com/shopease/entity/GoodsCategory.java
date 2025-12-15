package com.shopease.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品分类实体
 * @author hspcadmin
 */
@Data
@TableName("sys_goods_category") // 对应数据库表名
public class GoodsCategory {
    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO) // 自增主键
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID（0表示一级分类）
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间（MyBatis-Plus自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（MyBatis-Plus自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}