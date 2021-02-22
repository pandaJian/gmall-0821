package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallPmsApplicationTests {

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuAttrValueService skuAttrValueService;


    @Test
    void contextLoads() {
        System.out.println(skuAttrValueService.querySaleAttrsBySpuId(39l));
    }

}
