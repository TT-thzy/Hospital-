package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-22 21:15
 **/
@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {

    @Autowired
    private WeixinService weixinPayService;

    @Autowired
    private PaymentService paymentService;


    /**
     * 下单 生成二维码
     */
    @ApiOperation("生成支付二维码")
    @GetMapping("/createNative/{orderId}")
    public Result createNative(
            @PathVariable("orderId") Long orderId) {
        return Result.ok(weixinPayService.createNative(orderId));
    }

    @ApiOperation("查询支付状态")
    @GetMapping("/queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable Long orderId) {
        //调用查询支付状态接口
        Map<String, String> resultMap = weixinPayService.queryPayStatus(orderId, PaymentTypeEnum.WEIXIN.name());
        if (resultMap == null) {
            return Result.fail().message("支付失败");
        }
        if ("SUCCESS".equalsIgnoreCase(resultMap.get("trade_state"))) {     //支付成功
            //更改订单状态，处理支付结果
            String out_trade_no = resultMap.get("out_trade_no");
            paymentService.paySuccess(out_trade_no, PaymentTypeEnum.WEIXIN.getStatus(), resultMap);
            return Result.ok().message("支付成功");
        }
        //否则
        return Result.ok().message("支付中");
    }


}
