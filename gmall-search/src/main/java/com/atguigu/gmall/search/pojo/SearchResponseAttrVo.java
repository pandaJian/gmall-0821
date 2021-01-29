package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-01-29 20:31
 * Description
 */
@Data
public class SearchResponseAttrVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValues; //规格参数的可选值列表
}
