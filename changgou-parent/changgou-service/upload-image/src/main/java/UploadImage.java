import com.alibaba.fastjson.JSONObject;
import com.changgou.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@RestController
@RequestMapping(value = {"/upload"})
public class UploadImage {
    @RequestMapping(value = "/upload")
    public void upload(MultipartFile multipartFile) throws Exception{
        String url = "http://localhost:8080/upload/upload04";
        //发送请求
        HttpClient httpClient = new HttpClient(url);
        httpClient.setHttps(true);
        httpClient.setXmlParam(JSONObject.toJSONString(multipartFile));
        httpClient.post();
        //响应结果
        String result = httpClient.getContent();
        //解析结果xml2map
        System.out.println(result);
    }

}
