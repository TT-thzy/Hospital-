package com.atguigu.yygh.sms.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-11 18:38
 **/
@Component
public class ConstantPropertiesUtils implements InitializingBean {

    @Value("${message.host}")
    private String host;

    @Value("${message.path}")
    private String path;

    @Value("${message.method}")
    private String method;

    @Value("${message.appcode}")
    private String appcode;

    @Value("${message.template_id}")
    private String template_id;

    public static String HOST;
    public static String PATH;
    public static String METHOD;
    public static String APPCODE;
    public static String TEMPLATE_ID;

    @Override
    public void afterPropertiesSet() throws Exception {
        HOST=this.host;
        PATH=this.path;
        METHOD=this.method;
        APPCODE=this.appcode;
        TEMPLATE_ID=this.template_id;
    }
}
