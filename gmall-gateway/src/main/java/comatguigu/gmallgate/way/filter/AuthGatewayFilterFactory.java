package comatguigu.gmallgate.way.filter;

import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import comatguigu.gmallgate.way.config.JwtProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author : panda Jian
 * @date : 2021-02-22 19:23
 * Description
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {

    @Autowired
    private JwtProperties jwtProperties;

    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public GatewayFilter apply(PathConfig config) {

        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("这是局部过滤器，我只拦截特定路由对应的服务" + config.paths);

                //获取网关中的request和response对象
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                //1.判断当前请求的路径是否在拦截名单中，不在直接放行
                List<String> paths = config.paths;//获取拦截名单
                String curPath = request.getURI().getPath();
                if (CollectionUtils.isEmpty(paths) || !paths.stream().anyMatch(path -> StringUtils.startsWith(curPath,path))){
                    return chain.filter(exchange);
                }
                //2.获取请求中token，异步：头信息   同步：cookie
                String token = request.getHeaders().getFirst("token");
                if (StringUtils.isBlank(token)){
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if (!CollectionUtils.isEmpty(cookies) && cookies.containsKey(jwtProperties.getCookieName())){
                        token = cookies.getFirst(jwtProperties.getCookieName()).getValue();
                    }
                }
                //3.判断token信息是否为空，为空，则重定向到登陆页面
                if (StringUtils.isBlank(token)){
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();//拦截请求
                }
                try {
                    //4.使用公钥解析jwt，解析异常了，则重定向到登陆页面
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

                    //5.判断是自己的token:获取当前用户的ip地址，和载荷中的ip地址比较 不一致：重定向到登陆页面
                    String ip = map.get("ip").toString();//获取载荷中的ip地址
                    String curIp = IpUtils.getIpAddressAtGateway(request);
                    if (!StringUtils.equals(ip,curIp)){
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        return response.setComplete();//拦截请求
                    }
                    //6.把jwt中的登陆信息传递给后续服务，通过request头传递登陆信息
                    request.mutate().header("userId",map.get("userId").toString()).build();
                    exchange.mutate().request(request).build();
                    //7.放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();//拦截请求
                }
            }
        };
    }
    @Data
    public static class PathConfig{
        private List<String> paths;
    }
}
