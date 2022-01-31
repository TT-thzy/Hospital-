package com.atguigu.yygh.hosp.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-01-29 18:22
 **/
@Configuration
@MapperScan(basePackages = "com.atguigu.yygh.hosp.mapper")
public class HospitalSetConfig {
}
