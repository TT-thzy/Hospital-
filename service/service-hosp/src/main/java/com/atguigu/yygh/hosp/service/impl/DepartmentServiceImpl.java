package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.utils.BeanExchangeUtils;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-07 23:19
 **/
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> paramMap) {
        Department department = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Department.class);
        Department departmentExit = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());
        if (ObjectUtils.isEmpty(departmentExit)) {
            //添加
            department.setUpdateTime(new Date());
            department.setCreateTime(new Date());
            department.setIsDeleted(0);
        } else {
            //修改
            department.setUpdateTime(new Date());
            department.setCreateTime(departmentExit.getCreateTime());
            department.setIsDeleted(0);
        }
        departmentRepository.save(department);
    }

    @Override
    public Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //分页对象
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        //查询条件
        Department department = new Department();
        BeanExchangeUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching()//构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)//改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        //创建实例
        Example<Department> example = Example.of(department, matcher);
        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void removeDepartment(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (!ObjectUtils.isEmpty(department)) {
            departmentRepository.deleteById(department.getId());
        }
    }

    /**
     * @Description: 根据Hoscode查询该医院下面的所有科室并封装返回数据
     * @Param: [hoscode]
     * @return: java.util.List<com.atguigu.yygh.vo.hosp.DepartmentQueryVo>
     */
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //返回数据
        ArrayList<DepartmentVo> result = new ArrayList<>();
        //根据Hoscode查询该医院下面的所有科室
        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example = Example.of(department);
        List<Department> departmentList = departmentRepository.findAll(example);
        //根据大科室编号  bigcode 分组，获取每个大科室里面下级子科室
        Map<String, List<Department>> collect = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //封装返回结果
        for (Map.Entry<String, List<Department>> item : collect.entrySet()) {
            //大科室编号
            String bigCode = item.getKey();
            //大科室编号对应的分组数据
            List<Department> departments = item.getValue();
            //封装大科室
            DepartmentVo vo = new DepartmentVo();
            vo.setDepcode(bigCode);
            vo.setDepname(departments.get(0).getBigname());
            //封装小科室
            List<DepartmentVo> children = new ArrayList<>();
            for (Department departmentItem : departments) {
                DepartmentVo departmentVo = new DepartmentVo();
                departmentVo.setDepcode(departmentItem.getDepcode());
                departmentVo.setDepname(departmentItem.getDepname());
                children.add(departmentVo);
            }
            vo.setChildren(children);
            result.add(vo);
        }
        return result;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (ObjectUtils.isEmpty(department)) {
            return null;
        }
        return department.getDepname();
    }
}
