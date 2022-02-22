package com.atguigu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.oss.service.FileService;
import com.atguigu.yygh.oss.utils.ConstantOssPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * @program: yygh_parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-02-16 12:23
 **/
@Service
public class FileServiceImpl implements FileService {

    /*上传文件至阿里云oss，并返回该图片的url*/
    @Override
    public String upload(MultipartFile file) {
        String endpoint = ConstantOssPropertiesUtil.ENDPOINT;
        String bucket = ConstantOssPropertiesUtil.BUCKET;
        String accesskey = ConstantOssPropertiesUtil.ACCESSKEY;
        String secret = ConstantOssPropertiesUtil.SECRET;
        OSS ossClient = null;
        try {
            // 创建OSSClient实例。
            ossClient = new OSSClientBuilder().build(endpoint, accesskey, secret);
            //获取文件流
            InputStream inputStream = file.getInputStream();
            //文件名称
            String fileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            fileName = uuid + fileName;
            //按照当前日期，创建文件夹，上传到创建文件夹里面
            String module = new DateTime().toString("yyyy/MM/dd");
            fileName = module + "/" + fileName;
            //上传
            ossClient.putObject(bucket, fileName, inputStream);
            String url = "https://" + bucket + "." + endpoint + "/" + fileName;
            return url;
        } catch (Exception e) {
            throw new YyghException("文件上传失败", 500);
        } finally {
            ossClient.shutdown();
        }
    }
}
