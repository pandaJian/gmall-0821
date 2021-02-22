package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : panda Jian
 * @date : 2021-02-22 16:46
 * Description
 */
@Service
@EnableConfigurationProperties({JwtProperties.class})
public class AuthService {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private GmallUmsClient umsClient;

    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        try {
            //1.校验用户名和密码是否正确：调用远程接口
            ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUser(loginName, password);
            UserEntity userEntity = userEntityResponseVo.getData();
            //2.判断用户信息是否为空
            if (userEntity == null){
                throw new UserException("用户名或密码错误！！！");
            }
            //3.组织载荷
            Map<String,Object> map = new HashMap<>();
            map.put("userId",userEntity.getId());
            map.put("UserName",userEntity.getUsername());
            map.put("ip", IpUtils.getIpAddressAtService(request));
            //4.制作jwt
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            //5.需要把jwt放入cookie中
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpire() * 60);
            //6.为了方便展示用户的登录信息，需要写入unick
            CookieUtils.setCookie(request,response,jwtProperties.getUnick(),userEntity.getNickname(),jwtProperties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
