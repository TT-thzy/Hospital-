package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.util.List;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-05 12:23
 **/
@Api(tags = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {

    @Autowired
    private DictService dictService;

    @ApiOperation("根据数据id查询子数据列表")
    @GetMapping("/{id}")
    public Result getChildData(@PathVariable Long id) {
        List<Dict> dicts = dictService.getChildData(id);
        return Result.ok(dicts);
    }

    @ApiOperation("导出") //下载
    @GetMapping("/exportData")
    public void exportData(HttpServletResponse response) {
        dictService.exportData(response);
    }

    @ApiOperation("导入") //上传
    @PostMapping("/importData")
    public Result importData(MultipartFile file) {
        dictService.importData(file);
        return Result.ok();
    }

    //提供两个api接口，如省市区不需要上级编码，医院等级需要上级编码
    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getDictName(@PathVariable String parentDictCode, @PathVariable String value) {
        return dictService.getNameByParentDictCodeAndValue(parentDictCode, value);
    }

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{value}")
    public String getDictName(@PathVariable String value) {
        return dictService.getNameByParentDictCodeAndValue("", value);
    }

    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public Result<List<Dict>> findByDictCode(@PathVariable String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }

}
