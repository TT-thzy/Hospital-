package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.ConstantPropertiesUtils;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.atguigu.yygh.order.utils.HttpClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-22 21:16
 **/
@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RefundInfoService refundInfoService;

    @Override
    public Map createNative(Long orderId) {
        try {
            //如果缓存中存在该订单的二维码信息
            Map mapParam = (Map) redisTemplate.opsForValue().get(orderId.toString());
            if (mapParam != null) {
                return mapParam;
            }
            //根据id获取订单信息
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            // 保存交易记录
            paymentService.savePayment(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
            //1、设置参数
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = orderInfo.getReserveDate() + "就诊" + orderInfo.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");     //一分钱
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");
            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //client设置参数
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据
            String content = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            //4、封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));
            if (map.get("resultCode") != null) {
                //微信支付二维码2小时过期，可采取2小时未支付取消订单
                redisTemplate.opsForValue().set(orderId.toString(), map, 2, TimeUnit.HOURS);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap();
    }

    @Override
    public Map<String, String> queryPayStatus(Long orderId, String name) {
        try {
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            //1、封装参数
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //2、设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据，转成Map
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、返回
            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }

    //调用微信三方接口退款
    @Override
    public Boolean refund(Long orderId) {
        try {
            //查询支付记录
            PaymentInfo paymentInfo = paymentService.getPaymentInfoByOrderId(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            //保存退款记录
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus()) {
                return true;
            }
            //封装数据
            Map<String, String> paramMap = new HashMap<>(8);
            paramMap.put("appid", ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id", paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no", paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no", "tk" + paymentInfo.getOutTradeNo()); //商户退款单号
            // paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            // paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");
            paramMap.put("refund_fee", "1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            //访问微信第三方退款接口
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //返回第三方的数据
            String content = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            if (resultMap != null && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                //调用成功
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
