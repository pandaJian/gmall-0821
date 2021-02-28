package com.atguigu.gmall.order.feign;


import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author panda
 * @date 2021/2/27 11:51
 * Description
 **/
@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {
}
