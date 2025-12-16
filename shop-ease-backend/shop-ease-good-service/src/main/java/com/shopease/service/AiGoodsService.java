package com.shopease.service;

import com.shopease.dto.AiGoodsDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiGoodsService {

    private final ChatClient chatClient;

    @Value("${shopease.ai.goods.desc-template:请为【{categoryName}】类商品《{goodsName}》生成电商详情描述，卖点：{sellingPoint}。要求：200-300字，生动突出卖点。}")
    private String descTemplate;

    @Value("${shopease.ai.goods.title-template:请优化《{goodsName}》的副标题，原始：{originalSubTitle}，分类：{categoryName}，卖点：{sellingPoint}。要求：20字内，SEO友好，突出卖点。}")
    private String titleTemplate;

    // 构造器注入ChatClient.Builder并构建ChatClient
    public AiGoodsService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * AI生成商品详情描述
     * 关键修改：将text()替换为标准的user()方法
     */
    public String generateGoodDesc(AiGoodsDTO aiGoodsDTO) {
        // 封装参数
        Map<String, Object> params = new HashMap<>();
        params.put("categoryName", aiGoodsDTO.getCategoryName() == null ? "默认分类" : aiGoodsDTO.getCategoryName());
        params.put("goodsName", aiGoodsDTO.getGoodsName());
        params.put("sellingPoint", aiGoodsDTO.getSellingPoint() == null ? "无" : aiGoodsDTO.getSellingPoint());

        // 构建Prompt模板并渲染得到提示文本
        PromptTemplate promptTemplate = new PromptTemplate(descTemplate, params);
        String promptText = promptTemplate.render();

        // 核心修复：用user()替代text()，这是Spring AI 2.x的标准方法
        return chatClient
                .prompt() // 开始构建提示
                .user(promptText) // 设置用户的提示词（标准方法）
                .call() // 执行调用
                .content(); // 获取返回的内容字符串
    }

    /**
     * AI优化商品副标题
     */
    public String optimizeGoodTitle(AiGoodsDTO aiGoodsDTO) {
        // 封装参数
        Map<String, Object> params = new HashMap<>();
        params.put("goodsName", aiGoodsDTO.getGoodsName());
        params.put("categoryName", aiGoodsDTO.getCategoryName() == null ? "默认分类" : aiGoodsDTO.getCategoryName());
        params.put("sellingPoint", aiGoodsDTO.getSellingPoint() == null ? "无" : aiGoodsDTO.getSellingPoint());
        params.put("originalSubTitle", aiGoodsDTO.getOriginalSubTitle() == null ? "" : aiGoodsDTO.getOriginalSubTitle());

        // 渲染Prompt模板
        PromptTemplate promptTemplate = new PromptTemplate(titleTemplate, params);
        String promptText = promptTemplate.render();

        // 核心修复：用user()替代text()
        return chatClient
                .prompt()
                .user(promptText)
                .call()
                .content();
    }
}