package com.shopease.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopease.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}