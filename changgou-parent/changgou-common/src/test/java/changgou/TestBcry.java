package changgou;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Auther lxy
 * @Date
 */
public class TestBcry {
/**
*密码加密
 * @return : void
 */
    @org.junit.Test
    public void test(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String root = encoder.encode("java102");
        System.out.println(root);

    }
}
