package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.api.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : panda Jian
 * @date : 2021-02-23 20:14
 * Description
 */
@Service
public class CartService {

    @Autowired
    private CartAsyncService asyncService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallSmsClient smsClient;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    public void saveCart(Cart cart) {
        //1.获取用户登陆信息
        String userId = getUserId();

        //2.获取当前用户的购物车,内层map
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        //3.判断该用户的购物车是否包含该商品
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        if (hashOps.hasKey(skuId)) {
            //包含：更新数量
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));
            //用更新后的购物车对象覆盖redis中的对象
            asyncService.updateCart(cart,userId,skuId);
        }else {
            //不包含：新增一条记录
            cart.setUserId(userId);
            cart.setCheck(true);
            //查询sku相关信息
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null){
                return;
            }
            cart.setTitle(skuEntity.getTitle());
            cart.setPrice(skuEntity.getPrice());
            cart.setDefaultImage(skuEntity.getDefaultImage());
            //查询库存
            ResponseVo<List<WareSkuEntity>> listResponseVo = wmsClient.querySkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)){
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
            //查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));
            //查询营销信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));
            asyncService.insertCart(userId,cart);
            //加入购物车时加入价格缓存
            redisTemplate.opsForValue().set(PRICE_PREFIX + skuId,skuEntity.getPrice().toString());
        }
        hashOps.put(skuId,JSON.toJSONString(cart));
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = null;
        if (userInfo.getUserId() == null){
            return userInfo.getUserKey();
        }else {
            return userInfo.getUserId().toString();
        }
    }

    public Cart queryCartBySkuId(Long skuId) {
        String userId = this.getUserId();
        //根据外出key（userId，userKey）获取内存map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (hashOps.hasKey(skuId.toString())){
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson,Cart.class);
        }
        throw new CartException("该用户的购物车不包含该记录!!!");
    }

    public List<Cart> queryCarts() {
        //1.获取userKey
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        //组装key
        String unloginKey = KEY_PREFIX + userKey;
        //2.根据userKey查询未登陆的购物车
        //获取未登录购物车的内存的map
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(unloginKey);
        //获取未登录用户购物车的所有记录List<String>
        List<Object> unloginCartJsons = hashOps.values();
        List<Cart> unloginCarts = null;
        if (!CollectionUtils.isEmpty(unloginCartJsons)){
            unloginCarts = unloginCartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(redisTemplate.opsForValue().get(KEY_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }
        //3.获取userId
        Long userId = userInfo.getUserId();
        //4.如果userId为空，之间返回
        if (userId == null){
            return unloginCarts;
        }
        //5.把未登录的购物车合并到登陆状态的购物车
        String loginKey = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> loginHashOps = redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unloginCarts)){
            unloginCarts.forEach(cart -> { //未登录状态的购物车记录
                String skuId = cart.getSkuId().toString();
                BigDecimal count = cart.getCount();
                if (loginHashOps.hasKey(skuId)){
                    //如果用户的购物车包含了该记录，合并数量
                    String cartJson = loginHashOps.get(skuId).toString();//获取登陆购物车的对应记录
                    cart = JSON.parseObject(cartJson,Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    //写入redis 异步写入mysql
                    asyncService.updateCart(cart,userId.toString(),skuId);
                }else {
                    //用户的购物车不包含该记录，新增记录
                    cart.setUserId(userId.toString());
                    asyncService.insertCart(userId.toString(),cart);
                }
                loginHashOps.put(skuId,JSON.toJSONString(cart));//因为都需要写入redis
            });
        }
        //6.把未登录的购物车删除
        this.redisTemplate.delete(unloginKey);
        asyncService.deleteCart(userKey);
        //7.返回登陆状态的购物车
        List<Object> cartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(cartJsons)){
            return cartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }
        return null;
    }

    public void updateNum(Cart cart) {
        String userId = this.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (!hashOps.hasKey(cart.getSkuId().toString())){
            throw new CartException("该用户对应的购物车数据不存在");
        }
        BigDecimal count = cart.getCount();
        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
        cart = JSON.parseObject(cartJson,Cart.class);
        cart.setCount(count);
        hashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
        asyncService.updateCart(cart,userId,cart.getSkuId().toString());
    }

    public void deleteCart(Long skuId) {
        String userId = this.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (!hashOps.hasKey(skuId.toString())){
            throw new CartException("该用户对应的购物车数据不存在");
        }
        hashOps.delete(skuId.toString());
        asyncService.deleteCartBySkuId(userId,skuId);
    }

    public List<Cart> queryCheckedCarts(Long userId) {
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        List<Object> cartJsons = hashOps.values();
        if (CollectionUtils.isEmpty(cartJsons)){
            return null;
        }
        return cartJsons.stream()
                .map(cartJson -> JSON.parseObject(cartJson.toString(),Cart.class))
                .filter(Cart::getCheck)
                .collect(Collectors.toList());
    }
}
