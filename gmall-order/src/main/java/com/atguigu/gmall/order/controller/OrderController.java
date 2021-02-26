package com.atguigu.gmall.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author : panda Jian
 * @date : 2021-02-26 19:39
 * Description
 */
@Controller
public class OrderController {
    @GetMapping("confirm")
    public String confirm(Model model){

        return "trade";
    }
}
