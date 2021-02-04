package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : panda Jian
 * @date : 2021-02-02 21:11
 * Description
 */
@Component
public class GoodsListener {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GoodsRepository repository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SEARCH_INSERT_QUEUE",durable = "true"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        if (spuId == null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
        //查询spu
        ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(spuId);
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if (spuEntity == null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }

        ResponseVo<List<SkuEntity>> skuResponseVo = pmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> skuEntities = skuResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)){
            List<Goods> goodsList = skuEntities.stream().map(sku -> {
                Goods goods = new Goods();
                //创建时间
                goods.setCreateTime(spuEntity.getCreateTime());
                //sku相关信息
                goods.setSkuId(sku.getId());
                goods.setTitle(sku.getTitle());
                goods.setPrice(sku.getPrice().doubleValue());
                goods.setDefaultImage(sku.getDefaultImage());
                goods.setSubTitle(sku.getSubtitle());
                //获取库存：销量和是否有货
                ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.querySkuBySkuId(sku.getId());
                List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)){
                    goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a,b) -> a+b).get());
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                }
                //品牌
                ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(sku.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();
                if (brandEntity != null){
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }
                //分类
                ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsClient.queryCategoryById(sku.getCategoryId());
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                if (categoryEntity != null){
                    goods.setCategoryId(categoryEntity.getId());
                    goods.setCategoryName(categoryEntity.getName());
                }
                //检索参数
                List<SearchAttrValue> attrValues = new ArrayList<>();
                ResponseVo<List<SkuAttrValueEntity>> saleAttrValueResponseVo = pmsClient.querySearchAttrValuesByCidAndSkuId(sku.getCategoryId(), sku.getId());
                List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrValueResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                    attrValues.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        BeanUtils.copyProperties(skuAttrValueEntity,searchAttrValue);
                        return searchAttrValue;
                    }).collect(Collectors.toList()));
                }
                ResponseVo<List<SpuAttrValueEntity>> baseAttrValueResponseVo = pmsClient.querySearchAttrValuesByCidAndSpuId(sku.getCategoryId(), sku.getSpuId());
                List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrValueResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                    attrValues.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        BeanUtils.copyProperties(spuAttrValueEntity,searchAttrValue);
                        return searchAttrValue;
                    }).collect(Collectors.toList()));
                }
                goods.setSearchAttrs(attrValues);
                return goods;
            }).collect(Collectors.toList());
            repository.saveAll(goodsList);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
