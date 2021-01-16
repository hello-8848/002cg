package com.changgou.pay.controller;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
/**
 * @Auther lxy
 * @Date
 */
@RestController
@RequestMapping(value = "/image")
public class HttpPostUploadUtil {

   /**
   *测试方法
    * @param args :
    * @return : void
    */
    public static void main(String[] args) {
        String filePath = "C:\\Users\\luxueyi\\Desktop\\test01.jpg";
        String url = "http://localhost:8080/upload/upload04";
        //调用上传方法
        String backInfo = uploadPost(url, filePath);
        System.out.println(backInfo);
        System.out.println("");
    }
/**
*上传图片的接口
 * @param file :
 * @return : java.lang.String
 */
    @RequestMapping(value = "/upload")
    public  String uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        // 上传文件需要调用的接口
        String url = "http://localhost:8080/upload/upload04";
        // 获取文件名
        String originalFilename = file.getOriginalFilename();//test01.jpg
        String[] split = originalFilename.split("\\.");
        String newImageName = System.currentTimeMillis() + ".";
        String newFilename = newImageName + split[1];
        // 上传文件
        File fileUpload = null;//C:\Users\luxueyi\AppData\Local\Temp\1609644852271.5135577411289122458jpg
        fileUpload = File.createTempFile(newImageName, split[1]);
        String filePath = fileUpload.getAbsolutePath().split("\\.")[0] + "." + split[1];//C:\Users\luxueyi\AppData\Local\Temp\1609644852271.jpg
        fileUpload=new File(filePath);
        file.transferTo(fileUpload);
        fileUpload.deleteOnExit();
        String result = uploadPost2(url, fileUpload);
        // 返回前端保存图片的地址
        System.out.println(url);
        return "123456";
    }
  /**
  *上传图片
   * @param url :
   * @param file : 图片文件
   * @return : java.lang.String
   */
    private  String uploadPost2(String url, File file)  {

        // 创建请求体
        String requestJson = "";
        // 传入参数可以为file或者filePath，在此处做转换
        //File file = new File(filePath);
        // 创建httpclient
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;
        try {
            // 创建请求对象
            HttpPost httppost = new HttpPost(url);
            // 文件实体生成器
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // 设置浏览器兼容模式
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            // 设置请求的编码格式
            builder.setCharset(Consts.UTF_8);
            builder.setContentType(ContentType.MULTIPART_FORM_DATA);
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//加上此行代码解决返回中文乱码问题
            // 添加文件
            builder.addBinaryBody("file", file);
            // 构建请求实体
            HttpEntity reqEntity = builder.build();
            httppost.setEntity(reqEntity);
            // 执行请求
            httpResponse = httpClient.execute(httppost);
            // 获取请求结果
            int backCode = httpResponse.getStatusLine().getStatusCode();
            // 判断请求结果
            if(backCode == HttpStatus.SC_OK){
                HttpEntity httpEntity = httpResponse.getEntity();
                byte[] json= EntityUtils.toByteArray(httpEntity);
                requestJson = new String(json, "UTF-8");
                // 关闭流
                EntityUtils.consume(httpEntity);
                return requestJson;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 打印异常日志
        } finally {
            // 释放资源
            try {
                httpClient.close();
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
                // 打印异常日志
            }

        }
        return "success";
    }

/**
*上传图片
 * @param url :
 * @param filePath : 图片路径
 * @return : java.lang.String
 */
    private static String uploadPost(String url, String filePath)  {
        // 创建请求体
        String requestJson = "";
        // 传入参数可以为file或者filePath，在此处做转换
        File file = new File(filePath);
        // 创建httpclient
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;
        try {
            // 创建请求对象
            HttpPost httppost = new HttpPost(url);
            // 文件实体生成器
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // 设置浏览器兼容模式
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            // 设置请求的编码格式
            builder.setCharset(Consts.UTF_8);
            builder.setContentType(ContentType.MULTIPART_FORM_DATA);
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//加上此行代码解决返回中文乱码问题
            // 添加文件
            builder.addBinaryBody("file", file);
            // 构建请求实体
            HttpEntity reqEntity = builder.build();
            httppost.setEntity(reqEntity);
            // 执行请求
            httpResponse = httpClient.execute(httppost);
            // 获取请求结果
            int backCode = httpResponse.getStatusLine().getStatusCode();
            // 判断请求结果
            if(backCode == HttpStatus.SC_OK){
                HttpEntity httpEntity = httpResponse.getEntity();
                byte[] json= EntityUtils.toByteArray(httpEntity);
                requestJson = new String(json, "UTF-8");
                // 关闭流
                EntityUtils.consume(httpEntity);
                return requestJson;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 打印异常日志
        } finally {
            // 释放资源
            try {
                httpClient.close();
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
                // 打印异常日志
            }

        }
        return "success";
    }
}