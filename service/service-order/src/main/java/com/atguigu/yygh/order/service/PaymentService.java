package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-22 21:13
 **/
public interface PaymentService extends IService<PaymentInfo> {

    /**
     * 保存交易记录
     * @param order
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */

    void savePayment(OrderInfo order,Integer paymentType);

    void paySuccess(String out_trade_no, Integer paymentType, Map<String, String> resultMap);

    PaymentInfo getPaymentInfo(String out_trade_no, Integer paymentType);

    PaymentInfo getPaymentInfoByOrderId(Long orderId, Integer paymentType);
}
