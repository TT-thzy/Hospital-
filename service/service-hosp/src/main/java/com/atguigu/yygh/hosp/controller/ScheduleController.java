package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-08 00:03
 **/
@Api(tags = "医院排班接口")
@RestController
@RequestMapping("/api/hosp")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "上传排班")
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //安全校验
        String hoscode = (String) paramMap.get("hoscode");
        //参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        String sign = (String) paramMap.get("sign");
        String trueSign = hospitalSetService.getSign(hoscode);
        String encryptTrueSign = MD5.encrypt(trueSign);
        if (!sign.equals(encryptTrueSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        scheduleService.saveSchedule(paramMap);
        return Result.ok();
    }

    @ApiOperation(value = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result findSchedulePage(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //安全校验
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        //参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        String sign = (String) paramMap.get("sign");
        String trueSign = hospitalSetService.getSign(hoscode);
        String encryptTrueSign = MD5.encrypt(trueSign);
        if (!sign.equals(encryptTrueSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String) paramMap.get("limit"));
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        Page<Schedule> pageModel = scheduleService.selectPage(page, limit, scheduleQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "删除科室")
    @PostMapping("schedule/remove")
    public Result deleteSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //安全校验
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");
        //参数校验
        if (StringUtils.isEmpty(hoscode) || StringUtils.isEmpty(hosScheduleId)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        String sign = (String) paramMap.get("sign");
        String trueSign = hospitalSetService.getSign(hoscode);
        String encryptTrueSign = MD5.encrypt(trueSign);
        if (!sign.equals(encryptTrueSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        scheduleService.removeSchedule(hoscode, hosScheduleId);
        return Result.ok();
    }

}
