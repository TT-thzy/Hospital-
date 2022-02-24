package com.atguigu.yygh.order.service;

import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-22 21:16
 **/
public interface WeixinService {
    Map createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId, String name);

    Boolean refund(Long orderId);
}
