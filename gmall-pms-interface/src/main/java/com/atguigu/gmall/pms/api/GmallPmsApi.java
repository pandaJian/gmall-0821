package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-01-26 18:19
 * Description
 */
public interface GmallPmsApi {

    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategory(@PathVariable("parentId") Long parentId);

    @GetMapping("pms/category/parent/withsubs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLvl2CatesWithSubsByPid(@PathVariable("pid")Long pid);

    @GetMapping("pms/skuattrvalue/search/{cid}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValuesByCidAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("skuId") Long skuId);

    @GetMapping("pms/spuattrvalue/search/{cid}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValuesByCidAndSpuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId);
}
