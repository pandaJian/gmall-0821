package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建真实（已经存在的）目录
	private static final String pubKeyPath = "E:\\0821Java\\learn\\IdeaProject\\RSA\\rsa.pub";
    private static final String priKeyPath = "E:\\0821Java\\learn\\IdeaProject\\RSA\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 2);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MTM5Nzc2NTZ9.DZSptmqATM7HLcCr1OtiJg6bGO_yoUU08cBkQDEctrseTU0dpV0x9Up7591FU_Y6yQWF3Qxq41ORmamdxHESn_FKX4oNED7xfC1R4V2yCewJag_bw8BWAyDDLhlQti1NTcpOMrefHmvLDGd0j-TvI12j7rPCw-4ueJlk38LVFuuEF2wXzO0yymo0t2bHbq5OBvk6z8YXJNdMC7Hi6z8X7AN_wnsYFxNgHiPhOIY6RDXbblMMJEWahfoQ46a76sdS8MZp4QG1E_Zr9XDHOGDnjVdmkawo-a5CPShlCEHIfcjD2wDHyfBxW7Oo-uKh9gM_p4vErYmiNrgndZSjfe8mmg";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}