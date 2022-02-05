package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.esayExcelListener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.utils.BeanExchangeUtils;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-05 12:21
 **/
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    /**
     * @Description:根据数据id查询子数据列表
     * @Param: [id]
     * @return: java.util.List<com.atguigu.yygh.model.cmn.Dict>
     */
    @Transactional(propagation = Propagation.REQUIRED,readOnly = true)
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    @Override
    public List<Dict> getChildData(Long id) {

        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", id);
        List<Dict> dicts = baseMapper.selectList(dictQueryWrapper);
        //向list集合每个dict对象中设置hasChildren
        for (Dict dict : dicts) {
            dict.setHasChildren(hasChild(dict.getId()));
        }
        return dicts;
    }

    /**
     *@Description:判断id下面是否有子节点
     *@Param: [id]
     *@return: boolean
     */
    private boolean hasChild(Long id) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", id);
        Integer count = baseMapper.selectCount(dictQueryWrapper);
        return count > 0;
    }

    /**
     *@Description:导入
     *@Param: [file]
     *@return: void
     */
    @Transactional
    @CacheEvict(value = "dict", allEntries=true)
    @Override
    public void importData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *@Description:导出
     *@Param: [response]
     *@return: void
     */
    @Override
    public void exportData(HttpServletResponse response) {

        try {
            response.setContentType("application/vnd.ms-excel"); //设置响应类型
            response.setCharacterEncoding("utf-8"); //响应编码格式
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8"); //导出（下载）的文件名
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx"); //响应头

            List<Dict> dicts = baseMapper.selectList(null); //查询所有数据
            ArrayList<DictEeVo> dictEeVos = new ArrayList<>(dicts.size());
            //将dict转化成DictEeVo
            for (Dict dict : dicts) {
                DictEeVo dictEeVo = new DictEeVo();
                BeanExchangeUtils.copyProperties(dict, dictEeVo); //转换
                dictEeVos.add(dictEeVo);
            }
            //导出（下载）
            EasyExcel.write(response.getOutputStream(),DictEeVo.class).sheet("数据字典").doWrite(dictEeVos);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
