package com.shopease.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shopease.dto.GoodsQueryDTO;
import com.shopease.entity.Goods;
import com.shopease.vo.GoodsDetailVO;
import com.shopease.vo.GoodsListVO;

/**
 * 商品Service接口
 */
public interface GoodsService extends IService<Goods> {
    /**
     * 分页查询商品列表（支持分类筛选、关键词搜索）
     * @param queryDTO 查询条件
     * @return 商品列表VO分页
     */
    IPage<GoodsListVO> getGoodsPage(GoodsQueryDTO queryDTO);

    /**
     * 查询商品详情
     * @param id 商品ID
     * @return 商品详情VO
     */
    GoodsDetailVO getGoodsDetail(Long id);
}