package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author : panda Jian
 * @date : 2021-01-29 18:46
 * Description
 */
@Service
public class SearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    public SearchResponseVo search(SearchParamVo paramVo) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, builderDsl(paramVo));
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchResponseVo responseVo = parseResult(response);
            //分页参数
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());
            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SearchResponseVo parseResult(SearchResponse response){
        SearchResponseVo responseVo = new SearchResponseVo();
        //解析hits
        SearchHits hits = response.getHits();
        //总记录数
        responseVo.setTotal(hits.totalHits);
        //当前页的数据
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit -> {
            String json = hitsHit.getSourceAsString();
            Goods goods = JSON.parseObject(json, Goods.class);
            //获取高亮标题 换普通的标题
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
            goods.setTitle(highlightField.getFragments()[0].string());
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);
        //解析aggregations
        //把聚合结果集以map的形式解析，key聚合名称，value聚合内容
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        //获取品牌
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");//这里应该有问题
        List<? extends Terms.Bucket> brandIdAggBuckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(brandIdAggBuckets)){

            responseVo.setBrands(brandIdAggBuckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                //外层桶的key就是品牌id
                brandEntity.setId(bucket.getKeyAsNumber().longValue());
                //获取桶中的子聚合：品牌名称子聚合，品牌logo子聚合
                Map<String, Aggregation> brandSubAggMap = bucket.getAggregations().asMap();
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) brandSubAggMap.get("brandNameAgg");
                List<? extends Terms.Bucket> buckets = brandNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(buckets)){
                    //每个品牌名称子聚合中 有且仅有一个桶
                    brandEntity.setName(buckets.get(0).getKeyAsString());
                }
                //logo子聚合，有且仅有一个桶
                ParsedStringTerms logoAgg = (ParsedStringTerms) brandSubAggMap.get("logoAgg");
                List<? extends Terms.Bucket> buckets1 = logoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(buckets1)){
                    brandEntity.setLogo(buckets1.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList()));
        }
        //获取分类
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> buckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)){
            //把每个桶转换成每个分类
            List<CategoryEntity> categoryEntities = buckets.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(bucket.getKeyAsNumber().longValue());
                //获取分类名称的子聚合获取分类名称
                ParsedStringTerms categoryNameAgg = bucket.getAggregations().get("categoryNameAgg");
                categoryEntity.setName(categoryNameAgg.getBuckets().get(0).getKeyAsString());
                return categoryEntity;
            }).collect(Collectors.toList());
            responseVo.setCategories(categoryEntities);
        }
        //获取规格参数
        //获取规格参数的嵌套聚合
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        //获取嵌套聚合中attrId聚合
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        //获取attrId聚合中的桶集合，获取所有的检索类型的规格参数
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        //有些商品或者有些关键字，没有检索类型的规格参数，判空
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)){
            //把attrId聚合中的桶集合转化成List<SearchResponseAttrVo>
            List<SearchResponseAttrVo> searchResponseVos = attrIdAggBuckets.stream().map(bucket -> {
                //把每个桶转化成SearchResponseAttrVo对象
                SearchResponseAttrVo responseAttrVo = new SearchResponseAttrVo();
                //桶中的key就是attrId
                responseAttrVo.setAttrId(bucket.getKeyAsNumber().longValue());
                //获取子聚合获取attrName 和 attrValues
                Map<String, Aggregation> subAggMap = bucket.getAggregations().asMap();
                //获取attrName的子聚合
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) subAggMap.get("attrNameAgg");
                responseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                //获取attrValue的子聚合
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) subAggMap.get("attrValueAgg");
                List<? extends Terms.Bucket> valueBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(valueBuckets)){
                    responseAttrVo.setAttrValues(valueBuckets.stream().map(Terms.Bucket ::getKeyAsString).collect(Collectors.toList()));
                }
                return responseAttrVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(searchResponseVos);
        }
        return responseVo;
    }

    private SearchSourceBuilder builderDsl(SearchParamVo paramVo){
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String keyword = paramVo.getKeyword();
        if (StringUtils.isBlank(keyword)){
            // 打广告 赚钱的机会
            return sourceBuilder;
        }
        // 1.构建检索条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        // 1.1 构建搜索匹配条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));
        // 1.2 构建过滤条件
        // 1.2.1 构建品牌的过滤
        List<Long> brandId = paramVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",brandId));
        }
        // 1.2.2 构建分类的过滤
        List<Long> categoryId = paramVo.getCategoryId();
        if (!CollectionUtils.isEmpty(categoryId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId",categoryId));
        }
        // 1.2.3 构建价格区间的过滤
        Double priceFrom = paramVo.getPriceFrom();//起始价格
        Double priceTo = paramVo.getPriceTo();//终止价格
        if (priceFrom != null || priceTo != null){ //至少有一个不为空，才进行价格过滤
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (priceFrom != null){
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null){
                rangeQuery.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        // 1.2.4 构建是否有货的过滤
        Boolean store = paramVo.getStore();
        if (store != null && store){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store",store));
        }
        // 1.2.5 构建规格参数的嵌套过滤
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)){
            props.forEach(prop -> {
                //用冒号分割出attrId和attrValues字符串
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2){
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    //分割后的第一位就是attrId
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId",attr[0]));
                    //分割后第二位 8G-12G
                    String[] attrValues = StringUtils.split(attr[1], "-");
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue",attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs",boolQuery, ScoreMode.None));
                }
            });
        }
        // 2.排序
        Integer sort = paramVo.getSort();
        switch (sort){
            case 1:
                sourceBuilder.sort("price", SortOrder.DESC);
                break;
            case 2:
                sourceBuilder.sort("price",SortOrder.ASC);
                break;
            case 3:
                sourceBuilder.sort("sales",SortOrder.DESC);
                break;
            case 4:
                sourceBuilder.sort("createTime",SortOrder.DESC);
                break;
            default:
                sourceBuilder.sort("_score",SortOrder.DESC);
                break;
        }
        // 3.构建分页条件
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        // 4.构建高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red;'>").postTags("</font>"));
        // 5.构建聚合
        // 5.1 构建品牌的聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo"))
        );
        // 5.2 构建分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );
        // 5.3 构建规格参数的聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                )
        );
        // 6.构建结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId","defaultImag","price","title","subTitle"},null);
        return sourceBuilder;
    }
}
