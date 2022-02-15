package com.atguigu.yygh.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.JwtHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.util.ConstantPropertiesUtil;
import com.atguigu.yygh.user.util.HttpClientUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-15 15:19
 **/
@Api(tags = "微信登录接口")
@Controller
@RequestMapping("/api/ucenter/wx")
@Slf4j
public class WeChatController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("返回前端生成二维码需要的参数")
    @GetMapping("/getLoginParam")
    @ResponseBody
    public Result genQrConnect() {
        String redirectUri = null;
        try {
            redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis() + "");
        return Result.ok(map);
    }

    @ApiOperation("微信登陆回调接口(用户确认登陆后，重定向此处)")
    @GetMapping("/callback")
    public String callback(String code, String state) {   //这里的参数有微信提供 code:临时凭证，state:状态(一段随机数，防跨站脚本攻击)
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(state)) {
            log.error("非法回调请求");
            throw new YyghException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }
        //第一步 根据code，appid,appscrect,state访问微信提供的网址获取open_id和access_token
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        String result = null;
        try {
            result = HttpClientUtils.get(accessTokenUrl);
        } catch (Exception e) {
            log.error("获取accessToken失败");
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

//        System.out.println("使用code换取的access_token结果 = " + result);

        JSONObject resultJson = JSONObject.parseObject(result);
        if (resultJson.getString("errcode") != null) {
            log.error("获取access_token失败：" + resultJson.getString("errcode") + resultJson.getString("errmsg"));
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        String accessToken = resultJson.getString("access_token");
        String openId = resultJson.getString("openid");

        //第二步 根据open_id和access_token访问微信提供的网址获取用户信息,并保存
        UserInfo userInfo = userInfoService.findUserByOpenId(openId);
        if (userInfo == null) {
            //为空保存该用户信息，反之不需要保存
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openId);
            String resultUserInfo = null;
            try {
                resultUserInfo = HttpClientUtils.get(userInfoUrl);
            } catch (Exception e) {
                throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
            }
            System.out.println("使用access_token获取用户信息的结果 = " + resultUserInfo);

            JSONObject resultUserInfoJson = JSONObject.parseObject(resultUserInfo);
            if (resultUserInfoJson.getString("errcode") != null) {
                log.error("获取用户信息失败：" + resultUserInfoJson.getString("errcode") + resultUserInfoJson.getString("errmsg"));
                throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
            }
            //解析用户信息
            String nickname = resultUserInfoJson.getString("nickname");
//            String headimgurl = resultUserInfoJson.getString("headimgurl");

            userInfo = new UserInfo();
            userInfo.setNickName(nickname);
            userInfo.setOpenid(openId);
            userInfo.setStatus(1);
            userInfoService.save(userInfo);
        }
        //第三步 重定向到主页
        //返回name和token字符串
        Map<String, String> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);

        //判断userInfo是否有手机号，如果手机号为空，返回openid
        //如果手机号不为空，返回openid值是空字符串
        //前端判断：如果openid不为空，绑定手机号，如果openid为空，不需要绑定手机号
        if (StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", userInfo.getOpenid());
        } else {
            map.put("openid", "");
        }
//        System.out.println("openid"+map.get("openid"));
        //使用jwt生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        //跳转到前端页面
        String paramName = "";
        try {
            paramName = URLEncoder.encode(map.get("name"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "redirect:" + ConstantPropertiesUtil.YYGH_BASE_URL + "/weixin/callback?token=" + map.get("token") + "&openid=" + map.get("openid") + "&name=" + paramName;
    }
}
