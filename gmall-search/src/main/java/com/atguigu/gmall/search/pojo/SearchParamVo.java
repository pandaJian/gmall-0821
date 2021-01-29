package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-01-29 18:30
 * Description
 */
@Data
public class SearchParamVo {
    //检索关键字
    private String keyword;
    //品牌的过滤条件
    private List<Long> brandId;
    //分类的过滤条件
    private List<Long> categoryId;
    //规格参数的过滤
    private List<String> props;
    //排序字段: 0-默认、1-价格降序，2-价格升序、3-销量的降序、4-新品降序
    private Integer sort = 0;
    //价格区间过滤
    private Double priceFrom;
    private Double priceTo;
    //是否有货
    private Boolean store;
    //分页参数
    private Integer pageNum = 1;
    private final Integer pageSize = 10;
}
