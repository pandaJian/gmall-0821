package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author : panda Jian
 * @date : 2021-01-26 21:47
 * Description
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
