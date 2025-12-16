package com.shopease.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopease.entity.GoodsCategory;
import com.shopease.mapper.GoodsCategoryMapper;
import com.shopease.service.GoodsCategoryService;
import com.shopease.vo.GoodsCategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类Service实现类
 */
@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {

    /**
     * 构建分类树形结构
     */
    @Override
    public List<GoodsCategoryVO> getCategoryTree() {
        // 1. 查询所有分类
        List<GoodsCategory> categoryList = this.list();

        // 2. 转换为VO对象
        List<GoodsCategoryVO> categoryVOList = categoryList.stream().map(category -> {
            GoodsCategoryVO vo = new GoodsCategoryVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());

        // 3. 构建树形结构：先筛选一级分类（parentId=0），再为每个一级分类添加子分类
        List<GoodsCategoryVO> rootVOList = categoryVOList.stream()
                .filter(vo -> vo.getParentId() == 0) // 一级分类
                .collect(Collectors.toList());

        for (GoodsCategoryVO rootVO : rootVOList) {
            // 筛选当前一级分类的子分类（parentId=rootVO.getId()）
            List<GoodsCategoryVO> childrenVO = categoryVOList.stream()
                    .filter(vo -> vo.getParentId().equals(rootVO.getId()))
                    .collect(Collectors.toList());
            rootVO.setChildren(childrenVO);
        }

        return rootVOList;
    }
}