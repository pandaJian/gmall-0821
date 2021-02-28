package com.atguigu.gmall.oms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author : panda Jian
 * @date : 2021-02-28 12:59
 * Description
 */
public interface GmallOmsApi {
    @PostMapping("oms/order/save/{userId}")
    public ResponseVo saveOrder(@RequestBody OrderSubmitVo submitVo, @PathVariable("userId")Long userId);
}
