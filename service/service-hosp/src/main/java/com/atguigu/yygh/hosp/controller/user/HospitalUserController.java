package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-10 15:48
 **/
@Api(tags = "前台医院接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalUserController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(@PathVariable Integer page,
                        @PathVariable Integer limit,
                        HospitalQueryVo hospitalQueryVo
    ) {
        Page<Hospital> hospitals = hospitalService.selectPage(page, limit, hospitalQueryVo);
        return Result.ok(hospitals);
    }

    @ApiOperation(value = "根据医院名称获取医院列表")
    @GetMapping("/findByHosname/{hosname}")
    public Result findHospitalByHosname(@PathVariable String hosname) {
        List<Hospital> hospitals = hospitalService.getHospitalByHosname(hosname);
        return Result.ok(hospitals);
    }

    @ApiOperation(value = "医院预约挂号详情")
    @GetMapping("/{hoscode}")
    public Result item(@PathVariable String hoscode) {
        return Result.ok(hospitalService.item(hoscode));
    }


    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("/auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(
            @PathVariable Integer page,
            @PathVariable Integer limit,
            @PathVariable String hoscode,
            @PathVariable String depcode) {
        return Result.ok(scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode));
    }

    @ApiOperation(value = "获取该天排班数据")
    @GetMapping("/auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(
            @PathVariable String hoscode,
            @PathVariable String depcode,
            @PathVariable String workDate) {
        return Result.ok(scheduleService.getScheduleDetail(hoscode, depcode, workDate));
    }

    @ApiOperation(value = "根据排班id获取排班数据")
    @GetMapping("/getSchedule/{scheduleId}")
    public Result getSchedule(@PathVariable String scheduleId){
        return Result.ok(scheduleService.getScheduleById(scheduleId));
    }

    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId){
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("/inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable String hoscode){
        return hospitalService.getSignInfoVo(hoscode);
    }


}
