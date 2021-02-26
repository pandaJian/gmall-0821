package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-02-23 18:29
 * Description
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;


    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId")Long skuId){
        cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart){
        cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @GetMapping("cart.html")
    public String queryCarts(Model model){
        List<Cart> carts = cartService.queryCarts();
        model.addAttribute("carts",carts);
        return "cart";
    }

    @GetMapping
    public String saveCart(Cart cart){
        cartService.saveCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    @GetMapping("addCart.html")
    public String toCart(@RequestParam("skuId")Long skuId, Model model){
        Cart cart = cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart",cart);
        return "addCart";
    }

    @RequestMapping("test")
    @ResponseBody
    public String test(){
        System.out.println(LoginInterceptor.getUserInfo());
        return "Hello test";
    }
}
