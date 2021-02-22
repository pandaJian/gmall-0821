package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-02-02 23:57
 * Description
 */
@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping
    public String toIndex(Model model, HttpServletRequest request){
        System.out.println(request.getHeader("userId") + "============");
        //获取一级分类
        List<CategoryEntity>  categories = indexService.queryLvl1Categories();
        model.addAttribute("categories",categories);
        //获取广告信息
        return "index";
    }

    @GetMapping("/index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLv2CategoriesWithSubsByPid(@PathVariable("pid")Long pid){
        List<CategoryEntity> categoryEntities = indexService.queryLv2CategoriesWithSubsByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("index/test/lock")
    @ResponseBody
    public ResponseVo testLock(){
        indexService.testLock();
        return ResponseVo.ok();
    }

    @GetMapping("index/test/read")
    @ResponseBody
    public ResponseVo testRead(){
        indexService.read();
        return ResponseVo.ok("测试读方法------");
    }

    @GetMapping("index/test/write")
    @ResponseBody
    public ResponseVo testWrite(){
        indexService.write();
        return ResponseVo.ok("测试写方法======");
    }
}
