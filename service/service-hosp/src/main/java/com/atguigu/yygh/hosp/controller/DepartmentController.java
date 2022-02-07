package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-07 23:20
 **/
@Api(tags = "医院科室接口")
@RestController
@RequestMapping("/api/hosp")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("上传科室")
    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
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
        departmentService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation("分页查询科室")
    @PostMapping("/department/list")
    public Result findDepartmentPage(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //安全校验
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode"); //非必填
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
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        departmentQueryVo.setDepcode(depcode);
        Page<Department> departmentPage = departmentService.selectPage(page, limit, departmentQueryVo);
        return Result.ok(departmentPage);
    }

    @ApiOperation("删除科室")
    @PostMapping("/department/remove")
    public Result deleteDepartment(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //安全校验
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode"); //非必填
        //参数校验
        if (StringUtils.isEmpty(hoscode) || StringUtils.isEmpty(depcode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        String sign = (String) paramMap.get("sign");
        String trueSign = hospitalSetService.getSign(hoscode);
        String encryptTrueSign = MD5.encrypt(trueSign);
        if (!sign.equals(encryptTrueSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        departmentService.removeDepartment(hoscode, depcode);
        return Result.ok();
    }
}
