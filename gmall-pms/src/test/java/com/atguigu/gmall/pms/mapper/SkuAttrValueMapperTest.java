package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author : panda Jian
 * @date : 2021-02-19 21:05
 * Description
 */
@SpringBootTest
class SkuAttrValueMapperTest {
    @Autowired
    private SkuAttrValueMapper attrValueMapper;

    @Test
    void querySaleAttrsMappingSkuId() {
        System.out.println(attrValueMapper.querySaleAttrsMappingSkuId(Arrays.asList(51l, 52l, 53l, 54l)));
    }
}