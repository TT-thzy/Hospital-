package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.utils.BeanExchangeUtils;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
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
    private DictFeignClient dictFeignClient;

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

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        Hospital hospital = new Hospital();
        BeanExchangeUtils.copyProperties(hospitalQueryVo, hospital);
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> hospitals = hospitalRepository.findAll(example, pageable);
        hospitals.getContent().stream().forEach(item -> {
            this.packHospital(item);
        });
        return hospitals;
    }

    private void packHospital(Hospital item) {
        //封装等级名
        String hospTypeDataName = dictFeignClient.getDictName(DictEnum.HOSTYPE.getDictCode(), item.getHostype());
        item.getParam().put("hostypeString", hospTypeDataName);
        //封装省市区
        String cityString = dictFeignClient.getDictName(item.getCityCode());
        String provinceString = dictFeignClient.getDictName(item.getProvinceCode());
        String districtString = dictFeignClient.getDictName(item.getDistrictCode());
        item.getParam().put("fullAddress", provinceString + cityString + districtString + item.getAddress());
    }

    @Override
    public void updateStatus(String id, Integer status) {
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    @Override
    public Map<String, Object> getHospitalDetail(String id) {
        HashMap<String, Object> result = new HashMap<>();
        Hospital hospital = hospitalRepository.findById(id).get();
        this.packHospital(hospital);
        result.put("hospital", hospital);
        //单独处理更直观
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return result;

    }
}
