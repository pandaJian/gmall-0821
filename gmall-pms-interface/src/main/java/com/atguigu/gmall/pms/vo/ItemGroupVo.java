package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-02-19 18:22
 * Description
 */
@Data
public class ItemGroupVo {
    private Long id;
    private String name;
    private List<AttrValueVo> attrValue;
}
