package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.JwtHelper;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-10 21:45
 **/
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private PatientService patientService;

    //登录
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        String openid = loginVo.getOpenid();
        Map<String, Object> map = new HashMap<>();
        //参数校验
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 验证码校验
        String mobileCode = redisTemplate.opsForValue().get(phone);
        if (!code.equals(mobileCode)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }
        UserInfo userInfo = null;
        if (!StringUtils.isEmpty(openid)) {
            userInfo = this.findUserByOpenId(openid);
            if (userInfo != null) {
                //此处是防止数据库中已经有该手机号注册的账号
                QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
                wrapper.eq("phone", phone);
                //根据手机号查询会员
                UserInfo userInfoDelete = userInfoMapper.selectOne(wrapper);
                if (userInfoDelete != null) {
                    baseMapper.deleteById(userInfoDelete.getId());
                }
                //更新
                userInfo.setPhone(phone);
                baseMapper.updateById(userInfo);

            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        //根据手机号查询会员
        userInfo = userInfoMapper.selectOne(wrapper);
        if (userInfo == null) {
            //register
            UserInfo info = new UserInfo();
            info.setPhone(phone);
            info.setName("");
            info.setStatus(1);
            userInfoMapper.insert(info);
            UserInfo userResult = userInfoMapper.selectOne(wrapper);
            //校验是否被禁用
            if (userResult.getStatus() == 0) {
                throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
            }
            //登录
            //todo 记录登录信息
            //数据封装返回
            String name = userResult.getName();
            if (StringUtils.isEmpty(name)) {
                name = userResult.getNickName();
            }
            if (StringUtils.isEmpty(name)) {
                name = userResult.getPhone();
            }
            // token生成
            String token = JwtHelper.createToken(userResult.getId(), name);
            map.put("name", name);
            map.put("token", token);
            return map;
        } else {
            //校验是否被禁用
            if (userInfo.getStatus() == 0) {
                throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
            }
            //登录
            //todo 记录登录信息
            //数据封装返回
            String name = userInfo.getName();
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            // token生成
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("name", name);
            map.put("token", token);
            return map;
        }
    }

    @Override
    public UserInfo findUserByOpenId(String openId) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", openId);
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        return userInfo;
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        UserInfo userInfo = baseMapper.selectById(userId);
        if (userInfo != null) {
            //设置认证信息
            //认证人姓名
            userInfo.setName(userAuthVo.getName());
            //其他
            userInfo.setCertificatesType(userAuthVo.getCertificatesType());
            userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
            userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
            userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus()); //设置认证状态 认证中
            //更新
            baseMapper.updateById(userInfo);
        }
    }

    @Override
    public IPage<UserInfo> selectAllByPage(Long page, Long limit, UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> queryPage = new Page<>(page, limit);
        //UserInfoQueryVo获取条件值
        String name = userInfoQueryVo.getKeyword(); //用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间
        //对条件值进行非空判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            wrapper.like("name", name);
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);
        }
        if (!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status", authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time", createTimeEnd);
        }
        Page<UserInfo> userInfoPage = baseMapper.selectPage(queryPage, wrapper);
        userInfoPage.getRecords().stream().forEach(userInfo -> {
            this.packageUserInfo(userInfo);
        });
        return userInfoPage;
    }

    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态 0  1
        String statusString = userInfo.getStatus().intValue() == 0 ? "锁定" : "正常";
        userInfo.getParam().put("statusString", statusString);
        return userInfo;

    }

    /**
     * 用户锁定
     *
     * @param userId
     * @param status 0：锁定 1：正常
     */
    @Override
    public void lockOrUnlock(Long userId, Integer status) {
        if (status.intValue() == 0 || status.intValue() == 1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> UserInfoDetail(Long userId) {
        Map<String, Object> map = new HashMap<>();
        //根据userid查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        map.put("userInfo", userInfo);
        //根据userid查询就诊人信息
        List<Patient> patients = patientService.getAllPatient(userId);
        map.put("patientList", patients);
        return map;
    }

    //0 未认证 ， 1 认证中 ， 2 认证成功 ， -1 认证失败
    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus.intValue() == 2 || authStatus.intValue() == -1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }
}
