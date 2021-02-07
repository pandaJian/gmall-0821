package com.atguigu.gmall.index.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author : panda Jian
 * @date : 2021-02-06 17:47
 * Description
 */
@Component
@Slf4j
public class DistributedLock {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private Timer timer;

    public Boolean tryLock(String lockName,String uuid,Integer expire){
        String script = "if(redis.call('exists',KEYS[1]) == 0 or redis.call('hexists',KEYS[1],ARGV[1]) == 1) then " +
                "   redis.call('hincrby',KEYS[1],ARGV[1],1) " +
                "   redis.call('expire',KEYS[1],ARGV[2]) " +
                "   return 1 " +
                "else " +
                "   return 0 end";
        if (!redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString())){
            try {
                Thread.sleep(50);
                tryLock(lockName,uuid,expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 开启定时任务子线程 自动续期
        renewExpire(lockName,uuid,expire);
        return true;
    }

    public void unlock(String lockName,String uuid){
        // 返回值为nil 代表要解的锁 不存在 或者 要解别人的锁
        // 返回值为0 代表出来了一次
        // 返回值为1 代表解锁成功
        String script = "if(redis.call('hexists',KEYS[1],ARGV[1]) == 0) then " +
                "   return nil " +
                "elseif(redis.call('hincrby',KEYS[1],ARGV[1],-1) == 0) then " +
                "   return redis.call('del',KEYS[1]) else " +
                "   return 0 " +
                "end";
        // 此处的返回值不要使用bool 因为nil在布尔类型中会被解析成false
        Long flag = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid);
        if (flag == null){
            log.error("要解的锁不存在，或者要解别人的锁。锁的名称：{}，锁的uuid：{}",lockName,uuid);
        }else if(flag == 1){
            timer.cancel();
        }
    }

    private void renewExpire(String lockName,String uuid,Integer expire){
        String script ="if(redis.call('hexists',KEYS[1],ARGV[1]) == 1) then " +
                "   return redis.call('expire',KEYS[1],ARGV[2]) " +
                "else " +
                "   return 0 " +
                "end";
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //判断是不是自己的锁
                redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class),Arrays.asList(lockName),uuid,expire.toString());
            }
        },expire * 1000 / 3,expire * 1000 / 3);
    }
//    public static void main(String[] args) {
//        System.out.println("定时任务初始化时间：" + System.currentTimeMillis());
//        //jdk提供的定时器
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("jdk的定时器任务：" + System.currentTimeMillis());
//            }
//        },5000,10000);
//    }
}
