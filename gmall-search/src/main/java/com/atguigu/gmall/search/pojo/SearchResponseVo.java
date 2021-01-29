package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-01-29 20:27
 * Description
 */
@Data
public class SearchResponseVo {
    //品牌列表的渲染字段
    private List<BrandEntity> brands;
    //分类列表的渲染
    private List<CategoryEntity> categories;
    //规格参数的列表的渲染
    private List<SearchResponseAttrVo> filters;

    //分页
    private Integer pageNum;
    private Integer pageSize;

    private Long total;
    //当前页的具体数据
    private List<Goods> goodsList;

}
