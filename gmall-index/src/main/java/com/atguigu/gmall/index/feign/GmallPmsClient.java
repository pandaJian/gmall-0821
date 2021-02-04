package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author : panda Jian
 * @date : 2021-02-02 23:59
 * Description
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
