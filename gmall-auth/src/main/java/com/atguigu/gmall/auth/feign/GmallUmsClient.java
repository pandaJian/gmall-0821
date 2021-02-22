package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author : panda Jian
 * @date : 2021-02-22 16:46
 * Description
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
