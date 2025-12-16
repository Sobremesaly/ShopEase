package com.shopease.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopease.dto.GoodsQueryDTO;
import com.shopease.entity.Goods;
import com.shopease.mapper.GoodsMapper;
import com.shopease.vo.GoodsDetailVO;
import com.shopease.vo.GoodsListVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 商品Service实现类
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    /**
     * 分页查询商品列表
     */
    @Override
    public IPage<GoodsListVO> getGoodsPage(GoodsQueryDTO queryDTO) {
        // 1. 创建分页对象
        Page<GoodsListVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 2. 调用Mapper的自定义分页方法（关联分类名称）
        IPage<GoodsListVO> goodsPage = this.baseMapper.selectGoodsListPage(
                page,
                queryDTO.getCategoryId(),
                StringUtils.hasText(queryDTO.getKeyword()) ? queryDTO.getKeyword() : null,
                queryDTO.getStatus()
        );

        return goodsPage;
    }

    /**
     * 查询商品详情
     */
    @Override
    public GoodsDetailVO getGoodsDetail(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        GoodsDetailVO detailVO = this.baseMapper.selectGoodsDetailById(id);
        if (detailVO == null) {
            throw new RuntimeException("商品不存在");
        }
        return detailVO;
    }
}