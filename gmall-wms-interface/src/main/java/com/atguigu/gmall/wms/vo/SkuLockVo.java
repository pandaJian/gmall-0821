package com.atguigu.gmall.wms.vo;

import lombok.Data;

/**
 * @author : panda Jian
 * @date : 2021-02-27 23:21
 * Description
 */
@Data
public class SkuLockVo {
    private Long skuId;
    private Integer count;

    private Long wareSkuId; //锁定仓库的id

    //锁定状态
    private Boolean lock;
}
