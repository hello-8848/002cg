package com.jd.controller;

import com.jd.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * @Auther lxy
 * @Date
 */
@Controller
public class TestController {

    @RequestMapping(value = "/hello")
    public String hello(Model model) {
        model.addAttribute("test1", "hello java");
        model.addAttribute("test2", "<font style='color:red'>京东</font>");
        List<User> users = new ArrayList<>();
        users.add(new User(1,"张三","深圳"));
        users.add(new User(2,"李四","北京"));
        users.add(new User(3,"王五","武汉"));
        model.addAttribute("users",users);
        model.addAttribute("now", new Date());
        model.addAttribute("age", 20);
        model.addAttribute("age2", 17);
        Map<String,Object> dataMap = new HashMap<String,Object>();
        dataMap.put("No","123");
        dataMap.put("address","深圳");
        model.addAttribute("dataMap",dataMap);
        model.addAttribute("str", "abcd123");
        model.addAttribute("url", "/add");
        return "hello";
    }
    @RequestMapping("/add")
    public String add(String name,String address,Model model) {
        System.out.println(name+"  住址是 "+address);
        return "redirect:http://www.baidu.com";
    }
}
