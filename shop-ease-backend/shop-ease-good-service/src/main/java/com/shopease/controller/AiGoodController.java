package com.shopease.controller;


import com.shopease.dto.AiGoodsDTO; // 替换为AiGoodDTO
import com.shopease.result.Result;
import com.shopease.service.AiGoodsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI商品控制器（与Good模块对接的接口，product → good）
 */
@RestController
@RequestMapping("/api/ai/good") // product → good
public class AiGoodController { // AiProductController → AiGoodController

    private final AiGoodsService aiGoodService; // AiProductService → AiGoodService

    public AiGoodController(AiGoodsService aiGoodService) {
        this.aiGoodService = aiGoodService;
    }

    /**
     * AI生成商品描述接口（适配Good模块的新增/编辑接口）
     */
    @PostMapping("/generate-desc")
    public Result<String> generateGoodDesc(@Validated @RequestBody AiGoodsDTO aiGoodDTO) { // generateProductDesc → generateGoodDesc
        String desc = aiGoodService.generateGoodDesc(aiGoodDTO);
        return Result.success(desc);
    }

    /**
     * AI优化商品标题接口（适配Good模块的标题优化功能）
     */
    @PostMapping("/optimize-title")
    public Result<String> optimizeGoodTitle(@Validated @RequestBody AiGoodsDTO aiGoodDTO) { // optimizeProductTitle → optimizeGoodTitle
        String title = aiGoodService.optimizeGoodTitle(aiGoodDTO);
        return Result.success(title);
    }

}