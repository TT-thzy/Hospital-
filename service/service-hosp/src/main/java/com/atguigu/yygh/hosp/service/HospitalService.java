package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.vo.order.SignInfoVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @program: mybatisTest
 * @description:
 * @author: Mr.Wang
 * @create: 2021-03-27 17:26
 **/
public interface HospitalService {
    void saveHospital(Map<String, Object> map);

    Hospital getHospitalByHoscode(String hoscode);

    Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, Integer status);

    Map<String, Object> getHospitalDetail(String id);

    String getHospName(String hoscode);

    List<Hospital> getHospitalByHosname(String hosname);

    /**
     * 医院预约挂号详情
     */
    Map<String, Object> item(String hoscode);

    SignInfoVo getSignInfoVo(String hoscode);
}
