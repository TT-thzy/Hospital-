package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.DepartmentService;
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
 * @create: 2022-02-10 16:23
 **/
@Api(tags = "前台医院接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class DepartmentUserController {

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "获取科室列表")
    @GetMapping("/department/{hoscode}")
    public Result index(@PathVariable String hoscode){
        return Result.ok(departmentService.findDeptTree(hoscode));
    }

}
