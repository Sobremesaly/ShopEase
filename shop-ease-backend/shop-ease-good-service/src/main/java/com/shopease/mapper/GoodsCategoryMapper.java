package com.shopease.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopease.entity.GoodsCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类Mapper
 */
@Mapper
public interface GoodsCategoryMapper extends BaseMapper<GoodsCategory> {
}