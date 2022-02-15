package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.JwtHelper;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
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
}
