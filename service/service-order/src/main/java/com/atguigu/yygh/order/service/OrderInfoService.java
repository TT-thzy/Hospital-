package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @program: mybatisTest
 * @description:
 * @author: Mr.Wang
 * @create: 2021-03-27 17:26
 **/
public interface OrderInfoService extends IService<OrderInfo> {
    Long saveOrder(String scheduleId, Long patientId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> infoPage, OrderQueryVo orderQueryVo);

    OrderInfo getOrderDetail(String orderId);

    Map<String,Object> show(String id);

    Boolean cancelOrder(Long orderId);
    //就医提醒
    void patientTips();
}
