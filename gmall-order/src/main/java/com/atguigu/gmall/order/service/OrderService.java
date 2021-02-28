package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.pojo.OrderConfirmVo;
import com.atguigu.gmall.order.pojo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.api.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author : panda Jian
 * @date : 2021-02-27 11:53
 * Description
 */
@Service
public class OrderService {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallUmsClient umsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallCartClient cartClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "order:token:";

    public OrderConfirmVo confirm() {

        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //获取登陆用户id
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        //
        ResponseVo<List<Cart>> cartResponseVo = cartClient.queryCheckedCarts(userId);
        List<Cart> carts = cartResponseVo.getData();
        if (CollectionUtils.isEmpty(carts)){
            throw new OrderException("您没有选中的购物车记录");
        }
        List<OrderItemVo> itemVos = carts.stream().map(cart -> {
            OrderItemVo itemVo = new OrderItemVo();
            //只取购物车中的skuId和count，因为其他数据可能跟数据库实时数据不同
            itemVo.setSkuId(cart.getSkuId());
            itemVo.setCount(cart.getCount());
            //查询sku信息
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null){
                itemVo.setDefaultImage(skuEntity.getDefaultImage());
                itemVo.setTitle(skuEntity.getTitle());
                itemVo.setPrice(skuEntity.getPrice());
                itemVo.setWeigh(skuEntity.getWeight());
            }
            //查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrsResponseVo = pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> attrValueEntities = saleAttrsResponseVo.getData();
            itemVo.setSaleAttrs(attrValueEntities);
            //查询营销信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> saleVoList = salesResponseVo.getData();
            itemVo.setSales(saleVoList);
            //查询库存信息
            ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.querySkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)){
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
            return itemVo;
        }).collect(Collectors.toList());
        confirmVo.setOrderItems(itemVos);
        //根据用户id查询用户地址列表
        ResponseVo<List<UserAddressEntity>> addressResponseVo = umsClient.queryAddressByuserId(userId);
        List<UserAddressEntity> addressEntities = addressResponseVo.getData();
        confirmVo.setAddresses(addressEntities);

        //查询登陆用户的积分信息
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUserById(userId);
        UserEntity userEntity = userEntityResponseVo.getData();
        if (userEntity != null){
            confirmVo.setBounds(userEntity.getIntegration());
        }
        //生成一个唯一标识，防重，保存到redis中一份
        String orderToken = IdWorker.getTimeId();
        redisTemplate.opsForValue().set(KEY_PREFIX + orderToken,orderToken,24, TimeUnit.HOURS);
        confirmVo.setOrderToken(orderToken);
        return confirmVo;
    }

    public void submit(OrderSubmitVo submitVo) {
        //1.防重：redis
        String orderToken = submitVo.getOrderToken();
        if (StringUtils.isBlank(orderToken)){
            throw new OrderException("非法提交！！！");
        }
        String script = "if(redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(KEY_PREFIX + orderToken), orderToken);
        if (!flag){
            throw new OrderException("请不要重复提交!!!");
        }
        //2.验总价:遍历送货清单，获取数据库实时价格*数量，最后累加
        BigDecimal totalPrice = submitVo.getTotalPrice();//页面提交订单时的总价格
        List<OrderItemVo> items = submitVo.getItems();//订单中的送货清单
        if (CollectionUtils.isEmpty(items)){
            throw new OrderException("您没有要购买的商品！");
        }
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return new BigDecimal(0);
            }
            return skuEntity.getPrice().multiply(item.getCount());
        }).reduce((a, b) -> a.add(b)).get();
        if (currentTotalPrice.compareTo(totalPrice) != 0){
            throw new OrderException("您的页面已过期,请刷新后重试!");
        }
        //3.验库存并锁定库存（分布式锁）
        List<SkuLockVo> lockVos = items.stream().map(item -> {
            SkuLockVo lockVo = new SkuLockVo();
            lockVo.setSkuId(item.getSkuId());
            lockVo.setCount(item.getCount().intValue());
            return lockVo;
        }).collect(Collectors.toList());
        ResponseVo<List<SkuLockVo>> skuLockResponseVo = wmsClient.checkAndLock(lockVos, orderToken);
        List<SkuLockVo> skuLockVos = skuLockResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuLockVos)){
            throw new OrderException(JSON.toJSONString(skuLockVos));
        }
        //4.下单


        //5.删除购物车中对应的记录
    }
}
