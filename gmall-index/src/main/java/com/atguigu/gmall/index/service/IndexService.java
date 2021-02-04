package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author : panda Jian
 * @date : 2021-02-02 23:58
 * Description
 */
@Service
public class IndexService {

    private static final String KEY_PREFIX = "index:cates";

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;


    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryCategory(0l);
        return categoryResponseVo.getData();
    }

    public List<CategoryEntity> queryLv2CategoriesWithSubsByPid(Long pid) {
        //先查询缓存，如果缓存中有，直接返回
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json)){
            return JSON.parseArray(json,CategoryEntity.class);
        }
        //再去查询数据库，并放入缓存
        ResponseVo<List<CategoryEntity>> responseVo = pmsClient.queryLvl2CatesWithSubsByPid(pid);
        List<CategoryEntity> categoryEntities = responseVo.getData();

        //为了防止缓存穿透，空值null也进行缓存
        if (CollectionUtils.isEmpty(categoryEntities)){
            redisTemplate.opsForValue().set(KEY_PREFIX + pid,JSON.toJSONString(categoryEntities),5, TimeUnit.MINUTES);
        }else {
            //为了防止缓存雪崩，给缓存时间添加随机值
            redisTemplate.opsForValue().set(KEY_PREFIX + pid,JSON.toJSONString(categoryEntities),30 + new Random(10).nextInt(), TimeUnit.DAYS);
        }
        return categoryEntities;
    }

    public  void testLock() {
        //为了防止误删，添加唯一标识
        String uuid = UUID.randomUUID().toString();
        //加锁
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,3,TimeUnit.SECONDS);
        if (!lock){
            try {
                //如果获取锁失败，睡一会再去尝试获取锁
                Thread.sleep(50);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            String number = redisTemplate.opsForValue().get("number");
            if (number == null){
                return;
            }
            int num = Integer.parseInt(number);
            redisTemplate.opsForValue().set("number",String.valueOf(++num));
            //释放锁
            //判断是不是自己的速
            if (StringUtils.equals(uuid,redisTemplate.opsForValue().get("lock"))){
                redisTemplate.delete("lock");
            }
        }
    }
}
