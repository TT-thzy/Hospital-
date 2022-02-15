package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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



}
