package changgou;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
public class TestJWT {
    /**
    *创建jwt
     * @return : void
     */
    @Test
    public void testCreateJwt() {
        //创建一个jwt对象
        JwtBuilder builder = Jwts.builder();
        //设置唯一编号
        builder.setId("123456");
        builder.setSubject("小明");//设置猪蹄
        //builder.setExpiration(new Date(36000));//设置过期时间
        builder.setIssuedAt(new Date());////设置签发时间
        builder.signWith(SignatureAlgorithm.HS256, "java");//设置算法和加盐
        //存储自定义数据
        Map<String, Object> user = new HashMap<>();
        user.put("name", "zhangsan");
        user.put("age", 18);
        user.put("address", "北京");
        builder.addClaims(user);
        String compact = builder.compact();//构建
        System.out.println(compact);

    }
/**
*解析jwt
 * @return : void
 */
    @Test
    public void testParseJwt() {
        String s = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMjM0NTYiLCJzdWIiOiLlsI_mmI4iLCJpYXQiOjE2MDY4MDcxOTQsImFkZHJlc3MiOiLljJfkuqwiLCJuYW1lIjoiemhhbmdzYW4iLCJhZ2UiOjE4fQ.viQNSilPjRhSBXyYr2xmUe6k9QJDOQKsquHau_Q5iFA";
        Claims claims = Jwts.parser()
                .setSigningKey("java")//设置盐
                .parseClaimsJws(s)//解析jwt
                .getBody();//获得解析体
        System.out.println(claims);

    }
}
