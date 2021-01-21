package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.api.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author panda
 * @date 2021/1/20 20:53
 * Description
 **/
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/save")
    public ResponseVo saveSales(@RequestBody SkuSaleVo skuSaleVo);
}
