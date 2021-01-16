package com.changgou.controller;

import com.changgou.exception.ChanggouException;
import com.changgou.file.FastDFSFile;
import com.changgou.util.FastDFSClient;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Auther lxy
 * @Date
 */
@RestController
@CrossOrigin
public class FileController {
/**
 *文件上传
 * @param file :
 * @return : java.lang.String[]
 */
    @PostMapping(value = "/upload")
    public String upload(@RequestParam(value = "file") MultipartFile file) throws IOException {
        //创建实体类对象
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(),
                file.getBytes(),
                StringUtils.getFilenameExtension(file.getOriginalFilename()));
        //上传文件
        String[] strings = FastDFSClient.upload(fastDFSFile);
        return "http://images-changgou-java.itheima.net/"+strings[0]+"/"+strings[1];
    }


}
