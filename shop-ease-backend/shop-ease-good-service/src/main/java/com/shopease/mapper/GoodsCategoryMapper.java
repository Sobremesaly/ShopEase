package com.shopease.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopease.entity.GoodsCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 商品分类Mapper
 */
@Mapper
public interface GoodsCategoryMapper extends BaseMapper<GoodsCategory> {
    List<GoodsCategory> selectCategoryWithChildren(Long parentId);
}