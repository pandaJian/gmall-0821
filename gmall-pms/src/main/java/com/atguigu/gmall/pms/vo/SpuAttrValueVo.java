package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-01-20 13:56
 * Description
 */
public class SpuAttrValueVo extends SpuAttrValueEntity {

    private List<String> valueSelected;

    public void setValueSelected(List<String> valueSelected) {
        if (CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected,","));
    }
}
