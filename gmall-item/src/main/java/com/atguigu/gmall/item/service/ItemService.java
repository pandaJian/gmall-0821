package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValuesVo;
import com.atguigu.gmall.sms.api.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author : panda Jian
 * @date : 2021-02-19 23:04
 * Description
 */
@Service
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private TemplateEngine templateEngine;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();
        //获取sku的相关信息
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return null;
            }
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            return skuEntity;
        }, threadPoolExecutor);

        //设置分类信息
        CompletableFuture<Void> catesFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<CategoryEntity>> catesResponseVo = pmsClient.query123CategoriesByCid3(skuEntity.getCategoryId());
            List<CategoryEntity> categoryEntities = catesResponseVo.getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);

        //品牌信息
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);

        //spu信息
        CompletableFuture<Void> spuFature = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (skuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);

        //sku的图片列表
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> imagesResponseVo = pmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> imagesEntityList = imagesResponseVo.getData();
            itemVo.setImages(imagesEntityList);
        }, threadPoolExecutor);

        //sku营销信息
        CompletableFuture<Void> salesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> saleResponseVo = smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> sales = saleResponseVo.getData();
            itemVo.setSales(sales);
        }, threadPoolExecutor);

        //库存信息
        CompletableFuture<Void> storeFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.querySkuBySkuId(skuId);
            List<WareSkuEntity> wareResponseVoData = wareResponseVo.getData();
            if (CollectionUtils.isEmpty(wareResponseVoData)) {
                itemVo.setStore(wareResponseVoData.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPoolExecutor);

        //所有销售属性
        CompletableFuture<Void> saleAttrsFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<SaleAttrValuesVo>> saleAttrsResponseVo = pmsClient.querySaleAttrsBySpuId(skuEntity.getSpuId());
            List<SaleAttrValuesVo> saleAttrsResponseVoData = saleAttrsResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrsResponseVoData);
        }, threadPoolExecutor);

        //当前sku销售属性
        CompletableFuture<Void> saleAttrFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClient.querySaleAttrValuesBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                itemVo.setSaleAttr(skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue)));
            }
        }, threadPoolExecutor);

        //skuId和销售属性组合的映射关系
        CompletableFuture<Void> mappingFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<String> stringResponseVo = pmsClient.querySaleAttrsMappingSkuIdBySpuId(skuEntity.getSpuId());
            String json = stringResponseVo.getData();
            itemVo.setSkuJsons(json);
        }, threadPoolExecutor);

        //海报信息
        CompletableFuture<Void> descFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity descEntity = spuDescEntityResponseVo.getData();
            if (descEntity != null && StringUtils.isNotBlank(descEntity.getDecript())) {
                itemVo.setSpuImages(Arrays.asList(StringUtils.split(descEntity.getDecript(), ",")));
            }
        }, threadPoolExecutor);

        //分组及规格参数信息
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<ItemGroupVo>> groupResponseVo = pmsClient.queryGroupWithAttrValueBy(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuEntity.getId());
            List<ItemGroupVo> groupResponseVoData = groupResponseVo.getData();
            itemVo.setGroups(groupResponseVoData);
        }, threadPoolExecutor);

        //等待所有子任务执行完，才能返回
        CompletableFuture.allOf(catesFuture,brandFuture,spuFature,imagesFuture,salesFuture,storeFuture,
                saleAttrsFuture,saleAttrFuture,mappingFuture,descFuture,groupFuture).join();

        return itemVo;
    }

    public void generateHtml(ItemVo itemVo){
        //通过模板引擎生成静态页面，1-模块名称，2-上下文对象，3-文件流
        //初始化上下文对象，通过该对象给模板传递渲染所需要的数据
        Context context = new Context();
        context.setVariable("itemVo",itemVo);
        //初始化文件流
        try(PrintWriter printWriter = new PrintWriter("E:\\0821Java\\learn\\IdeaProject\\html\\" + itemVo.getSkuId() + ".html")){
            templateEngine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

//class CompletableFutureDemo{
//    public static void main(String[] args) throws IOException {
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("hello CompletableFuture");
//            //int i = 1 / 0;
//            return "hello supplyAsync";
//        });
//        CompletableFuture<String> future1 = future.thenApplyAsync(t -> {
//            System.out.println("=====thenApplyAsync1=====");
//            try {
//                TimeUnit.SECONDS.sleep(3);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("t = " + t);
//            return "hello thenApplyAsync1";
//        });
//        CompletableFuture<String> future2 = future.thenApplyAsync(t -> {
//            System.out.println("=====thenApplyAsync2=====");
//            try {
//                TimeUnit.SECONDS.sleep(2);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("t = " + t);
//            return "hello thenApplyAsync2";
//        });
//        CompletableFuture<String> future3 = future.thenApplyAsync(t -> {
//            System.out.println("=====thenApplyAsync3=====");
//            try {
//                TimeUnit.SECONDS.sleep(3);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("t = " + t);
//            return "hello thenApplyAsync3";
//        });
//        //CompletableFuture.anyOf(future1,future2,future3).join();
//        CompletableFuture.allOf(future1,future2,future3).join();
////                .whenCompleteAsync((t,u) -> {
////            System.out.println("t = " + t);
////            System.out.println("u = " + u);
////            System.out.println("执行另一个任务");
////        }).exceptionally(throwable -> {
////            System.out.println("throwable = " + throwable);
////            System.out.println("这是一个异常后的处理任务");
////            return "hello exceptionally";
////        });
//        try {
//            //System.out.println(future.get());
//            System.out.println("这是主方法的打印");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.in.read();
////        FutureTask<String> futureTask = new FutureTask<>(new MyCallable());
////        new Thread(futureTask).start();
////        try {
////            System.out.println(futureTask.get());
////            System.out.println("这是主线程的打印");
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//    }
//}
//
//class MyCallable implements Callable<String>{
//    @Override
//    public String call() throws Exception {
//        System.out.println("这是使用Callable初始化了多线程程序");
//        return "Hello Callable";
//    }
//}














