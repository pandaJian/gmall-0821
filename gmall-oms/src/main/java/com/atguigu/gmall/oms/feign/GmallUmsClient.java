package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author : panda Jian
 * @date : 2021-02-27 11:52
 * Description
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
