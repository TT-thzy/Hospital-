package com.atguigu.yygh.sms.service;

import com.atguigu.yygh.vo.msm.MsmVo;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-11 18:54
 **/
public interface MsmService {
    boolean sendMessage(String phone, String code);

    boolean send(MsmVo msmVo);
}
