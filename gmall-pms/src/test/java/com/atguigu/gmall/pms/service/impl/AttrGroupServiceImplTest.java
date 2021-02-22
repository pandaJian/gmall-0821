package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.service.AttrGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author : panda Jian
 * @date : 2021-02-19 22:59
 * Description
 */
@SpringBootTest
class AttrGroupServiceImplTest {

    @Autowired
    private AttrGroupService attrGroupService;

    @Test
    void queryGroupWithAttrValueBy() {
        System.out.println(attrGroupService.queryGroupWithAttrValueBy(225l, 20l, 27l));
    }
}