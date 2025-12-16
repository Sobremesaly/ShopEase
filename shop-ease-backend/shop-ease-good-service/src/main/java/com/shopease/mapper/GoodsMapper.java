package com.shopease.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopease.entity.Goods;
import com.shopease.vo.GoodsDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopease.vo.GoodsListVO;

/**
 * 商品Mapper接口
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    /**
     * 分页查询商品列表（关联分类名称）
     * @param page 分页对象
     * @param categoryId 分类ID
     * @param keyword 关键词
     * @param status 商品状态
     * @return 商品列表VO分页
     */
    IPage<GoodsListVO> selectGoodsListPage(
            Page<GoodsListVO> page,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

    /**
     * 查询商品详情（关联分类名称）
     * @param id 商品ID
     * @return 商品详情VO
     */
    GoodsDetailVO selectGoodsDetailById(@Param("id") Long id);
}