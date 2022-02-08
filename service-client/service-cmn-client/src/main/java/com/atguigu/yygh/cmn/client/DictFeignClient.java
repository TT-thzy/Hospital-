package com.atguigu.yygh.cmn.client;

import com.atguigu.yygh.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @program: mybatisTest
 * @description:
 * @author: Mr.Wang
 * @create: 2021-03-27 17:26
 **/
@FeignClient("service-cmn")
@Component
public interface DictFeignClient {

    @GetMapping(value = "/admin/cmn/dict/getName/{parentDictCode}/{value}")
    public String getDictName(@PathVariable("parentDictCode") String parentDictCode, @PathVariable("value") String value);

    @GetMapping(value = "/admin/cmn/dict/getName/{value}")
    public String getDictName(@PathVariable("value") String value);
}
