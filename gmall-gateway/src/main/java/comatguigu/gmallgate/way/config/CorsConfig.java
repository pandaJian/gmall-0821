package comatguigu.gmallgate.way.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author : panda Jian
 * @date : 2021-01-19 10:25
 * Description 配置跨域条件
 */
@Component
public class CorsConfig{

    @Bean
    public CorsWebFilter corsWebFilter(){
        //初始化CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        //允许的域，不能写*，否则cookie就无法使用
        config.addAllowedOrigin("http://manager.gmall.com");
        config.addAllowedOrigin("http://www.gmall.com");
        //允许的头信息
        config.addAllowedHeader("*");
        //允许的请求方式
        config.addAllowedMethod("*");
        //是否允许携带Cookie信息
        config.setAllowCredentials(true);
        //添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**",config);
        return new CorsWebFilter(corsConfigurationSource);
    }
}
