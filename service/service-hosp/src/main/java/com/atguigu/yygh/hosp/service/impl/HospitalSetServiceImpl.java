package com.atguigu.yygh.hosp.service.impl;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.hosp.mapper.HospitalSetMapper;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-01-29 18:12
 **/
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {

    @Override
    public String getSign(String hoscode) {
        HospitalSet hospitalSet = this.getSignByCode(hoscode);
        if (ObjectUtils.isEmpty(hospitalSet)){
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        if (hospitalSet.getStatus()==0){
            throw new YyghException(ResultCodeEnum.HOSPITAL_LOCK);
        }
        return hospitalSet.getSignKey();
    }

    private HospitalSet getSignByCode(String hoscode){
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode", hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(queryWrapper);
        return hospitalSet;
    }
}
