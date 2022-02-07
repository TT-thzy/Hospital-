package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * @program: mybatisTest
 * @description:
 * @author: Mr.Wang
 * @create: 2021-03-27 17:26
 **/
public interface DepartmentService {
    void save(Map<String, Object> paramMap);

    Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo);

    void removeDepartment(String hoscode, String depcode);
}
