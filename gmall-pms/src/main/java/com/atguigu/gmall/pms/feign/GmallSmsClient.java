package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author : panda Jian
 * @date : 2021-01-20 20:40
 * Description
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
