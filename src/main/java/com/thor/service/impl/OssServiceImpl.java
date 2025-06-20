package com.thor.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.thor.service.OssService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {
    @Value("${aliyun.oss.file.endpoint:}")
    private String endpoint;

    @Value("${aliyun.oss.file.keyId:}")
    private String accessKeyId;

    @Value("${aliyun.oss.file.keySecret:}")
    private String accessKeySecret;

    @Value("${aliyun.oss.file.bucketName:}")
    private String bucketName;

    // 阿里云上传图片
    public String uploadPhoto(MultipartFile file) {

        String url;

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 获取上传文件输入流
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 获取文件名称
        String fileName = file.getOriginalFilename();

        // 保证文件名唯一，去掉uuid中的'-'
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        fileName = uuid + fileName;

        // 把文件按日期分类，构建日期路径：avatar/2024/11/06/文件名
        // String datePath = new Date().toString("yyyy/MM/dd");
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        // 格式化日期
        String datePath = currentDate.format(formatter);
        // 拼接
        fileName = datePath + "/" + fileName;

        // 调用oss方法上传到阿里云
        // 第一个参数：Bucket名称
        // 第二个参数：上传到oss文件路径和文件名称
        // 第三个参数：上传文件输入流
        ossClient.putObject(bucketName, fileName, inputStream);

        // 把上传后把文件url返回
        // https://xppll.oss-cn-beijing.aliyuncs.com/01.jpg
        // "https://" + bucketName + "." + endpoint + "/" +
        url = fileName;
        // 关闭OSSClient
        ossClient.shutdown();

        return url;
    }

    // 删除图片
    @Override
    public void deletePhoto(String objectKey) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 调用deleteObject方法删除文件
            ossClient.deleteObject(bucketName, objectKey);
            System.out.println("File deleted successfully: " + objectKey);
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
        } finally {
            // 关闭OSSClient
            ossClient.shutdown();
        }
    }
}
