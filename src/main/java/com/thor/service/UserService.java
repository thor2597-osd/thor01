package com.thor.service;

import com.thor.entity.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.thor.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
public interface UserService extends IService<UserDO> {

    Result login(String userName,String passWord, HttpServletRequest request);

    Result register(String phone,String passWord,String email);

    Result uploadPhoto(MultipartFile file, int userId);

    Result uploadInfo(UserDO user);

    UserDO getUserById(long userId);
}
