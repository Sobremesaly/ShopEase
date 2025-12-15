package com.shopease.controller;

import com.shopease.result.Result;
import com.shopease.service.GoodsCategoryService;
import com.shopease.vo.GoodsCategoryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/goods/category")
public class GoodsCategoryController {

    @Resource
    private GoodsCategoryService goodsCategoryService;

    /**
     * 查询分类树形结构（一级分类+二级分类）
     */
    @GetMapping("/tree")
    public Result<List<GoodsCategoryVO>> getCategoryTree() {
        List<GoodsCategoryVO> categoryVOList = goodsCategoryService.getCategoryTree();
        return Result.success(categoryVOList);
    }
}