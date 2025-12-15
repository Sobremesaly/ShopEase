package com.shopease.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopease.dto.GoodsQueryDTO;
import com.shopease.entity.Goods;
import com.shopease.mapper.GoodsMapper;
import com.shopease.vo.GoodsDetailVO;
import com.shopease.vo.GoodsListVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Resource
    private GoodsCategoryService goodsCategoryService;

    /**
     * 分页查询商品列表（支持分类筛选、关键词搜索）
     */
    @Override
    public IPage<GoodsListVO> getGoodsPage(GoodsQueryDTO queryDTO) {
        // 1. 构建分页对象
        Page<Goods> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建查询条件（只查上架商品：status=1）
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, 1) // 只查上架商品
                .orderByAsc(Goods::getSort) // 按排序字段排序
                .orderByDesc(Goods::getCreateTime); // 按创建时间倒序

        // 3. 分类筛选（如果传入了categoryId）
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(Goods::getCategoryId, queryDTO.getCategoryId());
        }

        // 4. 关键词搜索（商品名称模糊匹配）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.like(Goods::getName, queryDTO.getKeyword());
        }

        // 5. 执行分页查询
        IPage<Goods> goodsPage = this.page(page, queryWrapper);

        // 6. 转换为GoodsListVO（封装分类名称）
        IPage<GoodsListVO> voPage = new Page<>(goodsPage.getCurrent(), goodsPage.getSize(), goodsPage.getTotal());
        List<GoodsListVO> voList = goodsPage.getRecords().stream()
                .map(goods -> {
                    GoodsListVO vo = new GoodsListVO();
                    BeanUtils.copyProperties(goods, vo);
                    // 处理商品图片（取第一个图片）
                    if (StringUtils.hasText(goods.getImage())) {
                        vo.setImage(goods.getImage().split(",")[0]);
                    }
                    // 封装分类名称
                    vo.setCategoryName(goodsCategoryService.getCategoryNameById(goods.getCategoryId()));
                    return vo;
                })
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询商品详情
     */
    @Override
    public GoodsDetailVO getGoodsDetail(Long id) {
        // 1. 查询商品信息（只查上架商品）
        Goods goods = this.getOne(
                new LambdaQueryWrapper<Goods>()
                        .eq(Goods::getId, id)
                        .eq(Goods::getStatus, 1)
        );
        if (goods == null) {
            throw new RuntimeException("商品不存在或已下架");
        }

        // 2. 转换为GoodsDetailVO
        GoodsDetailVO vo = new GoodsDetailVO();
        BeanUtils.copyProperties(goods, vo);
        // 处理商品图片列表（拆分逗号分隔的字符串）
        if (StringUtils.hasText(goods.getImage())) {
            List<String> imageList = Arrays.asList(goods.getImage().split(","));
            vo.setImageList(imageList);
        }
        // 封装分类名称
        vo.setCategoryName(goodsCategoryService.getCategoryNameById(goods.getCategoryId()));

        return vo;
    }
}