package com.atguigu.gmall.index.apect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.config.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author : panda Jian
 * @date : 2021-02-07 14:03
 * Description
 */
@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RBloomFilter bloomFilter;
//    @Pointcut("execution(* com.atguigu.gmall.index.service.*.*(..))")
//    public void yyy(){
//
//    }

    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //通过方法签名获取方法的返回结果集类型
        Class returnType = signature.getReturnType();
        //获取方法对象
        Method method = signature.getMethod();
        //获取方法上的注解对象
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        //获取注解对象中的前缀
        String prefix = gmallCache.prefix();
        //获取方法参数，返回的数组，而数组的toString是地址
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        //组装缓存的key
        String key = prefix + args;

        //防止缓存穿透，使用布隆过滤器
        if (!bloomFilter.contains(key)){
            return null;
        }

        //1.先查询缓存，如果缓存中命中，直接返回
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)){
            return JSON.parseObject(json,returnType);
        }
        //2.为了防止缓存击穿，添加分布式锁
        String lock = gmallCache.lock();
        RLock fairLock = redissonClient.getFairLock(lock + args);
        fairLock.lock();
        //3.在查询缓存，如果命中，直记返回
        String json2 = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json2)){
            return JSON.parseObject(json2,returnType);
        }
        //4.执行目标方法，远程调用或者从数据库中获取数据
        Object result = joinPoint.proceed(joinPoint.getArgs());
        //5.把数据放入缓存
        //获取缓存过期时间，为了防止缓存雪崩给过期时间添加随机值
        if (result != null){
            int timeout = gmallCache.timeout() + new Random().nextInt(gmallCache.random());
            redisTemplate.opsForValue().set(key,JSON.toJSONString(result),timeout, TimeUnit.MINUTES);
        }
        return result;
    }
}
