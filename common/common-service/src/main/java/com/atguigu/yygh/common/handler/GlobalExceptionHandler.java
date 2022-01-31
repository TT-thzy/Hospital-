package com.atguigu.yygh.common.handler;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @program: yygh_parent
 * @description: 全局异常处理
 * @author: Mr.Wang
 * @create: 2022-01-31 12:26
 **/
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result Exception(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }

    /**
     * @Description: 自定义异常处理
     * @Param: YyghException
     * @return: Result
     */
    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public Result YyghException(YyghException e) {
        e.printStackTrace();
        return Result.build(e.getCode(), e.getMessage());
    }
}

