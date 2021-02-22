package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.Set;

/**
 * @author : panda Jian
 * @date : 2021-02-19 16:52
 * Description
 */
@Data
public class SaleAttrValuesVo {
    private Long attrId;
    private String attrName;
    private Set<String> attrValues;
}
