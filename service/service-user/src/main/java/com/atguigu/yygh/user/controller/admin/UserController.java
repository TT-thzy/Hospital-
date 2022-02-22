package com.atguigu.yygh.user.controller.admin;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-16 21:52
 **/
@Api(tags = "后台用户管理接口")
@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("用户列表（条件查询带分页）")
    @GetMapping("/{page}/{limit}")
    public Result findAll(@PathVariable Long page, @PathVariable Long limit, UserInfoQueryVo userInfoQueryVo) {
        IPage<UserInfo> userInfoIPage = userInfoService.selectAllByPage(page, limit, userInfoQueryVo);
        return Result.ok(userInfoIPage);
    }

    @ApiOperation("用户解锁或锁定")
    @PutMapping("/lock/{userId}/{status}")
    public Result lockOrUnlock(@PathVariable Long userId, @PathVariable Integer status) {
        userInfoService.lockOrUnlock(userId, status);
        return Result.ok();
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/show/{userId}")
    public Result UserInfoDetail(@PathVariable Long userId) {
        Map<String, Object> map = userInfoService.UserInfoDetail(userId);
        return Result.ok(map);
    }

    @ApiOperation("认证审批")
    @PutMapping("/approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId, @PathVariable Integer authStatus) {
        userInfoService.approval(userId,authStatus);
        return Result.ok();
    }

}
