package com.secondhand.trading.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
  public String uploadPhoto(MultipartFile file);

  // 删除图片
  void deletePhoto(String objectKey);
}
