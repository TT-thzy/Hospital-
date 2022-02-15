package com.atguigu.yygh.sms.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.utils.RandomUtil;
import com.atguigu.yygh.sms.service.MsmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-11 18:54
 **/
@Api(tags = "短信接口")
@RestController
@RequestMapping("/api/msm")
public class MsmApiController {

    @Autowired
    private MsmService msmService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @ApiOperation("发送手机验证码")
    @GetMapping("/send/{phone}")
    public Result sendMessage(@PathVariable String phone) {
        //从redis获取验证码，如果获取获取到，返回ok
        // key 手机号  value 验证码
        String code = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(code)) {
            return Result.ok();
        }
        //如果从redis获取不到，
        // 生成验证码，
        code = RandomUtil.getSixBitRandom();
        //发送验证码
        boolean result = msmService.sendMessage(phone, code);
        //发送成功预估
        if (result){
            //生成验证码放到redis里面，设置有效时间
            redisTemplate.opsForValue().set(phone,code,1, TimeUnit.MINUTES);
            return Result.ok();
        }else {
            return Result.fail().message("短信发送失败，请稍后重试");
        }
    }

}
