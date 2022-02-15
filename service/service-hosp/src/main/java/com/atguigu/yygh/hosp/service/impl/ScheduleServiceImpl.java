package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.utils.BeanExchangeUtils;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-08 00:02
 **/
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;

    @Override
    public void saveSchedule(Map<String, Object> paramMap) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Schedule.class);
        Schedule scheduleExit = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        if (ObjectUtils.isEmpty(scheduleExit)) {
            //保存
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
        } else {
            //修改
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(scheduleExit.getCreateTime());
            schedule.setIsDeleted(0);
        }
        scheduleRepository.save(schedule);
    }

    @Override
    public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        //查询条件
        Schedule schedule = new Schedule();
        BeanExchangeUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching()//构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)//改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        //创建实例
        Example<Schedule> example = Example.of(schedule, matcher);
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void removeSchedule(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (!ObjectUtils.isEmpty(schedule)) {
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    //根据医院编号 和 科室编号 ，查询排班规则数据
    @Override
    public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {
        //查询条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //聚合操作
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),    //匹配条件
                Aggregation.group("workDate")   //分组
                        .first("workDate").as("workDate") //别名
                        .count().as("docCount") //医生数量
                        .sum("reservedNumber").as("reservedNumber") //总可预约数量
                        .sum("availableNumber").as("availableNumber"), //总剩余预约数量
                Aggregation.sort(Sort.Direction.ASC, "workDate"), //排序
                //分页
                Aggregation.skip(page - 1),
                Aggregation.limit(limit)
        );
        //查询
        AggregationResults<BookingScheduleRuleVo> aggregationResults = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVos = aggregationResults.getMappedResults();
        //分组查询的总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResults = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResults.getMappedResults().size();
        //把日期封装
        for (BookingScheduleRuleVo vo : bookingScheduleRuleVos) {
            Date workDate = vo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            vo.setDayOfWeek(dayOfWeek);
        }
        //设置最终数据，进行返回
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList",bookingScheduleRuleVos);
        result.put("total",total);

        //获取医院名称
        String hosName = hospitalService.getHospName(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hosName);
        result.put("baseMap",baseMap);
        return result;
    }

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        List<Schedule> list= scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());
        //把得到list集合遍历，向设置其他值：医院名称、科室名称、日期对应星期
        list.stream().forEach(item-> {
            this.packageSchedule(item);
        });
        return list;
    }

    private void packageSchedule(Schedule item) {
        //设置医院名称
        item.getParam().put("hosname",hospitalService.getHospName(item.getHoscode()));
        //设置科室名称
        item.getParam().put("depname",departmentService.getDepName(item.getHoscode(),item.getDepcode()));
        //设置日期对应星期
        item.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(item.getWorkDate())));

    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
