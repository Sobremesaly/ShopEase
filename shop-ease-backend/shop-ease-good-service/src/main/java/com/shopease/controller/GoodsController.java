package com.shopease.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shopease.dto.GoodsQueryDTO;
import com.shopease.result.Result;
import com.shopease.service.GoodsService;
import com.shopease.vo.GoodsDetailVO;
import com.shopease.vo.GoodsListVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    /**
     * 分页查询商品列表（支持分类筛选、关键词搜索）
     */
    @GetMapping("/list")
    public Result<IPage<GoodsListVO>> getGoodsList(GoodsQueryDTO queryDTO) {
        IPage<GoodsListVO> goodsPage = goodsService.getGoodsPage(queryDTO);
        return Result.success(goodsPage);
    }

    /**
     * 查询商品详情
     */
    @GetMapping("/{id}")
    public Result<GoodsDetailVO> getGoodsDetail(@PathVariable Long id) {
        GoodsDetailVO detailVO = goodsService.getGoodsDetail(id);
        return Result.success(detailVO);
    }
}