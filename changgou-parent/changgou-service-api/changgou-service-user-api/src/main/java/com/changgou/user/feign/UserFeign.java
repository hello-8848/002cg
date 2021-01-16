package com.changgou.user.feign;

import com.changgou.user.pojo.User;
import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Auther lxy
 * @Date
 */
@FeignClient(name = "user")
@RequestMapping(value = "/user")
public interface UserFeign {
    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<User> findById(@PathVariable(value = "id") String id);

    /**
     * 新增用户积分
     *
     * @param point :
     * @return : com.changgou.util.Result
     */
    @GetMapping(value = "/points/add")
    public Result incrUserPoints(Integer point);
}
