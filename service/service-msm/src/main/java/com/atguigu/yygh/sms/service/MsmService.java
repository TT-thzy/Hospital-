package com.atguigu.yygh.sms.service;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-11 18:54
 **/
public interface MsmService {
    boolean sendMessage(String phone, String code);
}
