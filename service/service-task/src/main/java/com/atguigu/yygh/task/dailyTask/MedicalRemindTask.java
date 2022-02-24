package com.atguigu.yygh.task.dailyTask;

import com.atguigu.yygh.common.rabbit.constant.MqConst;
import com.atguigu.yygh.common.rabbit.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @program: yygh_parent
 * @description: 每天早上8点就医提醒
 * @author: Mr.Wang
 * @create: 2022-02-24 13:01
 **/
@Component
@EnableScheduling
public class MedicalRemindTask {

    @Autowired
    private RabbitService rabbitService;

    /**
     * 每天8点执行 提醒就诊
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void medicalRemind(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "");
    }
}
