package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.rabbit.constant.MqConst;
import com.atguigu.yygh.common.rabbit.service.RabbitService;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.common.utils.BeanExchangeUtils;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.hospital.client.HospitalFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.atguigu.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-20 11:45
 **/
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeixinService weixinService;

    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        Patient patient = patientFeignClient.getPatient(patientId);
        if (null == patient) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        if (null == scheduleOrderVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //???????????????????????????
        if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());
        if (null == scheduleOrderVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        if (scheduleOrderVo.getAvailableNumber() <= 0) {
            throw new YyghException(ResultCodeEnum.NUMBER_NO);
        }
        OrderInfo orderInfo = new OrderInfo();
        BeanExchangeUtils.copyProperties(scheduleOrderVo, orderInfo);
        String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());   //?????????????????????
        this.save(orderInfo);
        //????????????
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", orderInfo.getHoscode());
        paramMap.put("depcode", orderInfo.getDepcode());
        paramMap.put("hosScheduleId", orderInfo.getScheduleId());
        paramMap.put("reserveDate", new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount", orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType", patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex", patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone", patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode", patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode", patient.getDistrictCode());
        paramMap.put("address", patient.getAddress());
        //?????????
        paramMap.put("contactsName", patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo", patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone", patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        paramMap.put("sign", signInfoVo.getSignKey());
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");
        if (result.getInteger("code") == 200) {
            JSONObject jsonObject = result.getJSONObject("data");
            //??????????????????????????????????????????????????????
            String hosRecordId = jsonObject.getString("hosRecordId");
            //????????????
            Integer number = jsonObject.getInteger("number");
            //????????????
            String fetchTime = jsonObject.getString("fetchTime");
            //????????????
            String fetchAddress = jsonObject.getString("fetchAddress");
            //????????????
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);
            //??????????????????
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //?????????????????????
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //??????mq?????????????????????????????????
            //??????mq??????????????????
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setAvailableNumber(availableNumber);

            //????????????
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
//            String reserveDate =
//                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
//                            + (orderInfo.getReserveTime() == 0 ? "??????" : "??????");
            Map<String, Object> param = new HashMap<String, Object>() {{
//                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
//                put("amount", orderInfo.getAmount());
//                put("reserveDate", reserveDate);
//                put("name", orderInfo.getPatientName());
//                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
                put("code", "order successful");
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        } else {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }

    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> infoPage, OrderQueryVo orderQueryVo) {
        //orderQueryVo???????????????
        String name = orderQueryVo.getKeyword(); //????????????
        Long patientId = orderQueryVo.getPatientId(); //???????????????
        String orderStatus = orderQueryVo.getOrderStatus(); //????????????
        String reserveDate = orderQueryVo.getReserveDate();//????????????
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        Long userId = orderQueryVo.getUserId();
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        //???????????????
        if (!StringUtils.isEmpty(userId)) {
            wrapper.eq("user_id", userId);
        }
        if (!StringUtils.isEmpty(name)) {
            wrapper.eq("hosname", name);
        }
        if (!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id", patientId);
        }
        if (!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status", orderStatus);
        }
        if (!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date", reserveDate);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time", createTimeEnd);
        }
        Page<OrderInfo> orderInfoPage = baseMapper.selectPage(infoPage, wrapper);
        //???????????????????????????
        orderInfoPage.getRecords().stream().forEach(orderInfo -> {
            this.packageOrderInfo(orderInfo);
        });
        return orderInfoPage;
    }

    @Override
    public OrderInfo getOrderDetail(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        return this.packageOrderInfo(orderInfo);
    }

    @Override
    public Map<String, Object> show(String id) {
        Map<String, Object> resultMap = new HashMap<>();
        OrderInfo orderInfo = this.packageOrderInfo(baseMapper.selectById(id));
        resultMap.put("orderInfo", orderInfo);
        Patient patient = patientFeignClient.getPatient(orderInfo.getPatientId());
        resultMap.put("patient", patient);
        return resultMap;
    }

    //????????????
    //1?????????????????????????????????????????????????????????????????????
    //2??????????????????????????????????????????????????????????????????????????????????????????
    @Override
    public Boolean cancelOrder(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        //???????????????????????????????????????
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if (quitTime.isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }
        //??????????????????????????????
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if (null == signInfoVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updateCancelStatus");
        if (result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        } else {
            if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus()) {    //?????????????????????
                //??????
                Boolean refund = weixinService.refund(orderInfo.getId());
                if (!refund) {
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
            }
            //1,2?????????????????????????????????,????????????????????????????????????
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            baseMapper.updateById(orderInfo);
            //??????mq????????????????????? ???????????????????????????????????????????????????mq????????????????????????????????????????????????????????????????????????+1??????
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(orderInfo.getScheduleId());
            //????????????
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
//            String reserveDate =
//                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
//                            + (orderInfo.getReserveTime() == 0 ? "??????" : "??????");
            Map<String, Object> param = new HashMap<String, Object>() {{
//                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
//                put("amount", orderInfo.getAmount());
//                put("reserveDate", reserveDate);
//                put("name", orderInfo.getPatientName());
//                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
                put("code", "cancel successful");
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        }
        return true;
    }

    //????????????
    @Override
    public void patientTips() {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("reserve_date",new DateTime().toString("yyyy-MM-dd"));
        List<OrderInfo> orderInfos = baseMapper.selectList(wrapper);
        for (OrderInfo orderInfo : orderInfos) {
            //????????????
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
//            String reserveDate =
//                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
//                            + (orderInfo.getReserveTime() == 0 ? "??????" : "??????");
            Map<String, Object> param = new HashMap<String, Object>() {{
//                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
//                put("amount", orderInfo.getAmount());
//                put("reserveDate", reserveDate);
//                put("name", orderInfo.getPatientName());
//                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
                put("code", "please seek medical treatment");
            }};
            msmVo.setParam(param);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

    private OrderInfo packageOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }
}
