package com.shopease.service;

import com.shopease.dto.AiGoodsDTO;
import com.shopease.dto.GoodsDTO;
import com.shopease.entity.Goods;
import com.shopease.mapper.GoodsMapper;
import org.springframework.stereotype.Service;

/**
 * 商品业务服务（核心逻辑不变，因@Data自动生成Getter/Setter，调用不受影响）
 */
@Service
public class GoodService {

    private final GoodsMapper goodsMapper;
    private final AiGoodsService aiGoodsService;

    // 构造器注入
    public GoodService(GoodsMapper goodsMapper, AiGoodsService aiGoodsService) {
        this.goodsMapper = goodsMapper;
        this.aiGoodsService = aiGoodsService;
    }

    /**
     * 新增商品（集成AI生成详情、优化副标题）
     */
    public void addGoods(GoodsDTO goodsDTO) {
        // 1. 转换DTO为AI参数DTO（@Data生成的getter可正常调用）
        AiGoodsDTO aiGoodsDTO = new AiGoodsDTO();
        aiGoodsDTO.setGoodsName(goodsDTO.getName());
        aiGoodsDTO.setCategoryName(goodsDTO.getCategoryName());
        aiGoodsDTO.setSellingPoint(goodsDTO.getSellingPoint());
        aiGoodsDTO.setOriginalSubTitle(goodsDTO.getSubTitle());

        // 2. AI生成描述（若前端未传）
        if (goodsDTO.getDescription() == null || goodsDTO.getDescription().isEmpty()) {
            String aiDescription = aiGoodsService.generateGoodDesc(aiGoodsDTO);
            goodsDTO.setDescription(aiDescription);
        }

        // 3. AI优化副标题
        String aiSubTitle = aiGoodsService.optimizeGoodTitle(aiGoodsDTO);
        goodsDTO.setSubTitle(aiSubTitle);

        // 4. 转换DTO为实体（@Data生成的setter可正常调用）
        Goods goods = new Goods();
        goods.setCategoryId(goodsDTO.getCategoryId());
        goods.setName(goodsDTO.getName());
        goods.setSubTitle(goodsDTO.getSubTitle());
        goods.setPrice(goodsDTO.getPrice());
        goods.setStock(goodsDTO.getStock());
        goods.setImage(goodsDTO.getImage());
        goods.setDescription(goodsDTO.getDescription());
        goods.setStatus(goodsDTO.getStatus() == null ? 1 : goodsDTO.getStatus()); // 默认上架
        goods.setSort(goodsDTO.getSort() == null ? 0 : goodsDTO.getSort()); // 默认排序0

        // 5. 保存到数据库
        goodsMapper.insert(goods);
    }
}