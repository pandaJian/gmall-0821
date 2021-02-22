package comatguigu.gmallgate.way.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author : panda Jian
 * @date : 2021-02-22 15:13
 * Description
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String cookieName;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    /**
     * 读取公钥和私钥赋值给公钥和私钥对象
     */
    @PostConstruct
    public void init(){
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
