package com.shopease.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopease.entity.GoodsCategory;
import com.shopease.mapper.GoodsCategoryMapper;
import com.shopease.service.GoodsCategoryService;
import com.shopease.vo.GoodsCategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现
 */
@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {

    /**
     * 查询分类树形结构（一级分类+二级分类）
     */
    @Override
    public List<GoodsCategoryVO> getCategoryTree() {
        // 1. 查询所有分类（按parentId和sort排序）
        List<GoodsCategory> categoryList = this.list(
                new LambdaQueryWrapper<GoodsCategory>()
                        .orderByAsc(GoodsCategory::getParentId)
                        .orderByAsc(GoodsCategory::getSort)
        );

        // 2. 筛选一级分类（parentId=0）
        List<GoodsCategoryVO> parentVOList = categoryList.stream()
                .filter(category -> category.getParentId() == 0)
                .map(category -> {
                    GoodsCategoryVO vo = new GoodsCategoryVO();
                    BeanUtils.copyProperties(category, vo);
                    // 3. 为每个一级分类添加子分类（二级分类）
                    List<GoodsCategoryVO> childrenVOList = categoryList.stream()
                            .filter(child -> child.getParentId().equals(category.getId()))
                            .map(child -> {
                                GoodsCategoryVO childVO = new GoodsCategoryVO();
                                BeanUtils.copyProperties(child, childVO);
                                return childVO;
                            })
                            .collect(Collectors.toList());
                    vo.setChildren(childrenVOList);
                    return vo;
                })
                .collect(Collectors.toList());

        return parentVOList;
    }

    /**
     * 根据分类ID查询分类名称
     */
    @Override
    public String getCategoryNameById(Long categoryId) {
        GoodsCategory category = this.getById(categoryId);
        return category == null ? "" : category.getName();
    }
}