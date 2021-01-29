package com.atguigu.gmall.search.search;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author : panda Jian
 * @date : 2021-01-29 18:43
 * Description
 */
@Controller
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    @ResponseBody
    public ResponseVo<SearchResponseVo> search(SearchParamVo searchParamVo){
        SearchResponseVo responseVo = searchService.search(searchParamVo);
        return ResponseVo.ok(responseVo);
    }
}
