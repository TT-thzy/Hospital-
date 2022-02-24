package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.hospital.client.HospitalFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-22 21:14
 **/
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Override
    public void savePayment(OrderInfo order, Integer paymentType) {
        //先判断交易记录表中是否已经存在该交易记录
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", order.getId());
        wrapper.eq("payment_type", paymentType);
        PaymentInfo paymentResult = baseMapper.selectOne(wrapper);
        if (paymentResult != null) {
            return;
        }
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd") + "|" + order.getHosname() + "|" + order.getDepname() + "|" + order.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(order.getAmount());
        baseMapper.insert(paymentInfo);
    }

    //支付成功
    @Override
    public void paySuccess(String out_trade_no, Integer paymentType, Map<String, String> resultMap) {
        PaymentInfo paymentInfo = this.getPaymentInfo(out_trade_no, paymentType);
        if (paymentInfo == null) {  //查询是否有该交易记录
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        if (paymentInfo.getPaymentStatus() != PaymentStatusEnum.UNPAID.getStatus()) {   //已经支付
            return;
        }
        //修改支付状态
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(resultMap.toString());
        baseMapper.updateById(paymentInfo);
        //修改订单状态
        OrderInfo orderInfo = orderInfoService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);
        // 调用医院接口，通知更新支付状态
        SignInfoVo signInfoVo
                = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if (null == signInfoVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");
        if (result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
    }

    public PaymentInfo getPaymentInfo(String out_trade_no, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", out_trade_no);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public PaymentInfo getPaymentInfoByOrderId(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }


}
