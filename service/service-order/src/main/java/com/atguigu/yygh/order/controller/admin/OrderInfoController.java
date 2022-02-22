package com.atguigu.yygh.order.controller.admin;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-21 11:43
 **/
@Api(tags = "订单管理接口")
@RestController
@RequestMapping("/admin/order/orderInfo")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result getAllOrderInfos(@PathVariable Long page, @PathVariable Long limit, OrderQueryVo orderQueryVo) {
        Page<OrderInfo> infoPage = new Page<>(page, limit);
        IPage<OrderInfo> pageResult = orderInfoService.selectPage(infoPage, orderQueryVo);
        return Result.ok(pageResult);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    @ApiOperation(value = "获取订单")
    @GetMapping("/show/{id}")
    public Result getOrderInfoDetail(@PathVariable String id){
        return Result.ok(orderInfoService.show(id));
    }



}
