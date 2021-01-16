package com.changgou;

import com.changgou.util.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Auther lxy
 * @Date
 */
@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.changgou.goods.dao"})
public class GoodsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class, args);
    }


    /**
     * 生成主键id的方法
     *参数1代表idwork编号,参数2代表任意值
     * @return : com.changgou.util.IdWorker
     */
    @Bean
    public IdWorker idWorker() {
        return new IdWorker(0,1);
    }
}
