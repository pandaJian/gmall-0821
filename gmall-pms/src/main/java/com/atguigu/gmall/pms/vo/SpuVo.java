package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-01-20 13:51
 * Description
 */
@Data
public class SpuVo extends SpuEntity {
    private List<String> spuImages;
    private List<SpuAttrValueVo> baseAttrs;
    private List<SkuVo> skus;
}
