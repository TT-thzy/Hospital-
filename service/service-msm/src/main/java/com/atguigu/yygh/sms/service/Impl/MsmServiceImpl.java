package com.atguigu.yygh.sms.service.Impl;


import com.atguigu.yygh.common.utils.HttpUtils;
import com.atguigu.yygh.sms.service.MsmService;
import com.atguigu.yygh.sms.utils.ConstantPropertiesUtils;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-11 18:55
 **/
@Service
public class MsmServiceImpl implements MsmService {


    @Override
    public boolean sendMessage(String phone, String code) {
        if (phone.isEmpty() || code.isEmpty()) {
            return false;
        }
        boolean send = this.send(code, phone);
        return send;
    }

    private boolean send(String code, String phone) {
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + ConstantPropertiesUtils.APPCODE);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("content", "code:" + code);
        bodys.put("phone_number", phone);
        bodys.put("template_id", ConstantPropertiesUtils.TEMPLATE_ID);


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(ConstantPropertiesUtils.HOST, ConstantPropertiesUtils.PATH, ConstantPropertiesUtils.METHOD, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
//            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
