package com.atguigu.yygh.hosp.controller.Api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.Hospital;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
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
 * @create: 2022-02-07 17:15
 **/
@Api(tags = "医院管理Api接口")
@RestController
@RequestMapping("/api/hosp")
public class HospitalApiController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("上传医院")
    @PostMapping("/saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //安全校验
        String hoscode = (String) map.get("hoscode");
        //参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        String sign = (String) map.get("sign");
        String trueSign = hospitalSetService.getSign(hoscode);
        String encryptTrueSign = MD5.encrypt(trueSign);
        if (!sign.equals(encryptTrueSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoDataString = (String) map.get("logoData");
        if (!StringUtils.isEmpty(logoDataString)) {
            String logoData = logoDataString.replaceAll(" ", "+");
            map.put("logoData", logoData);
        }
        hospitalService.saveHospital(map);
        return Result.ok();
    }

    @ApiOperation("获取医院信息")
    @PostMapping("/hospital/show")
    public Result findHospital(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验
        String hoscode = (String) paramMap.get("hoscode");
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
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        if (ObjectUtils.isEmpty(hospital)) {
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        return Result.ok(hospital);
    }

}
