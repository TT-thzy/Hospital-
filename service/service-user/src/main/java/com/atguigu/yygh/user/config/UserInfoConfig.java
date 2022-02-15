package com.atguigu.yygh.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-10 21:47
 **/
@Configuration
@MapperScan("com.atguigu.yygh.user.mapper")
public class UserInfoConfig {

}
