package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author : panda Jian
 * @date : 2021-01-26 21:47
 * Description
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
