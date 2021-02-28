package com.atguigu.gmall.wms.api;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author panda
 * @date 2021/1/26 18:38
 * Description
 **/
public interface GmallWmsApi {

    @GetMapping("wms/waresku/sku/{skuId}")
    public ResponseVo<List<WareSkuEntity>> querySkuBySkuId(@PathVariable Long skuId);

    @PostMapping("wms/waresku/check/lock/{orderToken}")
    public ResponseVo<List<SkuLockVo>> checkAndLock(@RequestBody List<SkuLockVo> lockVos, @PathVariable("orderToken")String orderToken);
}
