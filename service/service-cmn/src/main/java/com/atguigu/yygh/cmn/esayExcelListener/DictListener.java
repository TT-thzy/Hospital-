package com.atguigu.yygh.cmn.esayExcelListener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.common.utils.BeanExchangeUtils;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-05 16:48
 **/
public class DictListener extends AnalysisEventListener<DictEeVo> {

    private DictMapper dictMapper;

    public DictListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    //跳过表头,每读取一行就执行该方法
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //每读取一行就将其保存到数据库中
        Dict dict = new Dict();
        BeanExchangeUtils.copyProperties(dictEeVo, dict);
        dictMapper.insert(dict);
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
