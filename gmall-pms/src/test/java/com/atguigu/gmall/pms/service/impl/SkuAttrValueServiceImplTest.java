package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author : panda Jian
 * @date : 2021-02-19 21:25
 * Description
 */
@SpringBootTest
class SkuAttrValueServiceImplTest {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Test
    void queryPage() {
    }

    @Test
    void querySearchAttrValuesByCidAndSkuId() {
    }

    @Test
    void querySaleAttrsBySpuId() {
    }

    @Test
    void querySaleAttrsMappingSkuIdBySpuId() {
        System.out.println(skuAttrValueService.querySaleAttrsMappingSkuIdBySpuId(39l));
    }
}