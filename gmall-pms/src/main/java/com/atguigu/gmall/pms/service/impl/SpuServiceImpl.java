package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpiAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.api.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    SpuDescMapper spuDescMapper;
    @Autowired
    SpuAttrValueService spuAttrValueService;
    @Autowired
    SkuMapper skuMapper;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuAttrValueService skuAttrValueService;
    @Autowired
    GmallSmsClient gmallSmsClient;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }
    @Override
    public PageResultVo querySpuInfo(PageParamVo pageParamVo, Long categoryId) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (categoryId != 0){
            wrapper.eq("category_id",categoryId);
        }
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)){
            wrapper.and(t -> t.eq("id",key).or().like("name",key));
        }
        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                wrapper
        );
        return new PageResultVo(page);
    }

    @Override
    public void bigSave(SpuVo spu) {
        //1.保存spu相关信息
        //1.1保存spu表信息
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        Long spuId = spu.getId();
        //1.2保存spu_desc表信息
        List<String> spuImages = spu.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)){
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setDecript(StringUtils.join(spuImages,","));
            spuDescEntity.setSpuId(spuId);
            spuDescMapper.insert(spuDescEntity);
        }
        //1.3保存spu_attr_value表信息
        List<SpiAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){
            List<SpuAttrValueEntity> attrValueEntities = baseAttrs.stream().map(spiAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spiAttrValueVo,spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuId);
                return spuAttrValueEntity;
            }).collect(Collectors.toList());
            spuAttrValueService.saveBatch(attrValueEntities);
        }

        //2.保存sku相关信息
        //2.1保存sku表信息
        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(sku -> {
            sku.setSpuId(spuId);
            sku.setBrandId(spu.getBrandId());
            sku.setCategoryId(spu.getCategoryId());
            //设置默认图片
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)){
                sku.setDefaultImage(StringUtils.isNotBlank(sku.getDefaultImage()) ? sku.getDefaultImage():images.get(0));
            }
            skuMapper.insert(sku);
            Long skuId = sku.getId();
            //2.2保存sku_images信息
            if (!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(sku.getDefaultImage(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);
            }
            //2.3保存sku_attr_value表信息
            List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(skuAttrValueEntity -> skuAttrValueEntity.setSkuId(skuId));
                skuAttrValueService.saveBatch(saleAttrs);
            }
            //3.最后保存营销相关的信息（需要远程调用）
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(sku,skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            gmallSmsClient.saveSales(skuSaleVo);
        });
    }

}