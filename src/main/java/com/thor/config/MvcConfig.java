package com.thor.config;

import com.thor.util.LoginOrRefreshInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

  @Resource
  private LoginOrRefreshInterceptor loginOrRefreshInterceptor;

//  @Override
//  public void addInterceptors(@NonNull InterceptorRegistry registry) {
//    registry.addInterceptor(loginOrRefreshInterceptor)
//        .excludePathPatterns(
//            "/api/User/login",
//            "/api/User/register"
//        );
//  }
}