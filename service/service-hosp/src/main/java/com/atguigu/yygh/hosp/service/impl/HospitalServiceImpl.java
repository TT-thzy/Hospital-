package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-07 17:14
 **/
@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private HospitalSetService hospitalSetService;

    @Override
    public void saveHospital(Map<String, Object> map) {
        //将参数转换成Hospital
        String HospitalString = JSONObject.toJSONString(map);
        Hospital hospital = JSONObject.parseObject(HospitalString, Hospital.class);
        //唯一标志
        String hoscode = hospital.getHoscode();
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hoscode);
        //判断Mongo中是否存在该Hospital
        if (ObjectUtils.isEmpty(hospitalByHoscode)) {
            //保存
            //0：未上线 1：已上线
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
        } else {
            //修改
            hospital.setStatus(hospitalByHoscode.getStatus());
            hospital.setCreateTime(hospitalByHoscode.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
        }
        hospitalRepository.save(hospital);
    }

    @Override
    public Hospital getHospitalByHoscode(String hoscode) {
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospitalByHoscode;
    }
}
