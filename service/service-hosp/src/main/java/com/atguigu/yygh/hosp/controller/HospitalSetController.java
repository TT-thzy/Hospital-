package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.status.Status;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-01-29 18:14
 **/

@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    @GetMapping
    @ApiOperation(value = "获取所有医院设置")
    public Result findAll() {
        List<HospitalSet> list = hospitalSetService.list();
        if (list != null) {
            return Result.ok(list);
        } else {
            return Result.fail(list);
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "逻辑删除医院设置")
    public Result removeById(@PathVariable("id") Long id) {
        boolean b = hospitalSetService.removeById(id);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation(value = "带条件的分页查询")
    @PostMapping("/{current}/{limit}")
    public Result findHospitalSetWithPage(@PathVariable long current,
                                          @PathVariable long limit,
                                          @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
        //创建page对象，传递当前页，每页记录数
        Page<HospitalSet> page = new Page<>(current, limit);
        //构建条件
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        //查询条件
        String hoscode = hospitalSetQueryVo.getHoscode();
        String hosname = hospitalSetQueryVo.getHosname();
        if (!StringUtils.isEmpty(hosname)) {
            queryWrapper.like("hosname", hosname);
        }
        if (!StringUtils.isEmpty(hoscode)) {
            queryWrapper.eq("hoscode", hoscode);
        }
        Page<HospitalSet> hospitalSetPage = hospitalSetService.page(page, queryWrapper);
        return Result.ok(hospitalSetPage);
    }

    @ApiOperation(value = "添加医院设置")
    @PostMapping
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet) {
        //设置状态 1 使用 0 不能使用
        hospitalSet.setStatus(Status.open);
        //签名秘钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "") + random.nextInt(1000));
        //保存
        boolean b = hospitalSetService.save(hospitalSet);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation("根据id获取医院设置信息")
    @GetMapping("{id}")
    public Result queryHospitalSetById(@PathVariable long id) {
        //全局异常测试
        /*try {
            int i = 1 / 0;
        } catch (Exception e) {
            throw new YyghException("算术异常",500);
        }*/
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    @ApiOperation("批量删除医院信息")
    @DeleteMapping
    public Result deleteHospitalSetWithBatch(@RequestBody List<Long> idList) {
        boolean b = hospitalSetService.removeByIds(idList);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }

    }


}
