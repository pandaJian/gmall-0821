package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author : panda Jian
 * @date : 2021-01-26 22:00
 * Description
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
