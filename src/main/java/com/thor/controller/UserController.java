package com.thor.controller;

import com.thor.entity.UserDO;
import com.thor.model.Result;
import com.thor.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Controller
@RequestMapping("/api/User")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    @ResponseBody
    public Result login(@RequestParam("userName") String userName, @RequestParam("passWord") String passWord, HttpServletRequest request){
        // 登录功能的实现
        return userService.login(userName,passWord,request);
    }

    @PostMapping("/register")
    @ResponseBody
    public Result register(@RequestParam(value = "phone", required = false)String phone,@RequestParam("passWord")String passWord,@RequestParam(value = "email", required = false)String email){
        // 注册功能的实现
        return userService.register(phone,passWord,email);
    }

    @PostMapping("/updatePhoto")
    @ResponseBody
    public Result updatePhoto(@RequestParam("file") MultipartFile file, @RequestParam("userId") int userId) {
        // 用户修改自己的头像
        return userService.uploadPhoto(file,userId);
    }

    @PostMapping("/updateInfo")
    @ResponseBody
    public Result updateInfo(@RequestBody UserDO user) {
        // 用户修改个人信息
        return userService.uploadInfo(user);
    }

    // 每次打开app都会调用这个请求
    @RequestMapping(value = "/refresh", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result refresh() {
        return Result.success();
    }

}
