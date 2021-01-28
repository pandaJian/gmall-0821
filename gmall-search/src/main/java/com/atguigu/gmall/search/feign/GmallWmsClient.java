package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author : panda Jian
 * @date : 2021-01-26 21:47
 * Description
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
