package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-10 21:46
 **/
@Api(tags = "前台用户接口")
@RestController
@RequestMapping("/api/user")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginVo loginVo){
        Map<String,Object> result=userInfoService.login(loginVo);
        return Result.ok(result);
    }


}
