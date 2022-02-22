package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-10 21:43
 **/
public interface UserInfoService extends IService<UserInfo> {
    Map<String, Object> login(LoginVo loginVo);

    UserInfo findUserByOpenId(String openId);

    void userAuth(Long userId, UserAuthVo userAuthVo);

    IPage<UserInfo> selectAllByPage(Long page, Long limit, UserInfoQueryVo userInfoQueryVo);

    void lockOrUnlock(Long userId, Integer status);

    Map<String,Object> UserInfoDetail(Long userId);

    void approval(Long userId, Integer authStatus);
}
