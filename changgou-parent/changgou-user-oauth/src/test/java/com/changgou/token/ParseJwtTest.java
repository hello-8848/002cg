package com.changgou.token;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

/*****
 * @Author: shenkunlin
 * @Date: 2019/7/7 13:48
 * @Description: com.changgou.token
 *  使用公钥解密令牌数据
 ****/
public class ParseJwtTest {

    /***
     * 校验令牌
     */
    @Test
    public void testParseToken(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfVklQLFJPTEVfVVNFUiIsIm5hbWUiOiJpdGhlaW1hIiwiaWQiOiIxIn0.edcia4Xr20go9UN3RzP0lA9IdiABN42eZRKVjX9Jy0E0re9GvhHGn7k1CxsSO-jR4yiNRtlo6-zfG2DMc0q1rRJcO656q7FUhWw7PQqdjtfofF7BYw0TOVO9WRn5hAq3pso3IvBwoGLGOLWcF_VvQDgvxPo83YdYSdVhSwX5Cmw2edXQHx1vKVAwhhfEQPE_BewiUGwfM50idedccaZaOLnj7i5uI4FOmdv2qwTtqks4PoLJ5QA9tEyHt3MyzorGOLVqQByC-Eyii2t0daiJ2TEoAWhG8Drh26tOFO6nSi3imPnheGPB9zt4clOoehA_z8mQglZguh4FNFqjduJWIQ";

        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjBN1aQpsBLoD+5XXu0VhUkogfvtpZ24/h0twijeqYk1dWyTLr35cT2a/15ib4eZ5bD+LMbLhSSKcqFpH1dEpbJf6kVikJDODNGfYun+veABRP2SPHCCekP78zGEuljuNVc6TdPiHQ7GaBNoPj3iAW3aY5EeyJyUTFV6IX+LoDJICHcXvQOYnTBpU6P3h0aPp9bSETvSzqIsMx7wuSRTA9CSIMvHRpTJGsOqcMymdR7kb+L31GKJT931ujLCMw984IH902vJHEK19InVCbIiBvmi812wWszoIfVJhEV+vWsB65rWUobqsXQ2T9r+kEyxz76nHmCkAL45enWI78+YLxwIDAQAB-----END PUBLIC KEY-----";

        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        //获取Jwt原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
