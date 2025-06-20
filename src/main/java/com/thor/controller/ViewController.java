package com.thor.controller;

import com.thor.model.Result;
import com.thor.service.ViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Controller
@RequestMapping("/api/View")
@RequiredArgsConstructor
public class ViewController {

    private final ViewService viewService;

    // 添加到历史浏览
    @PostMapping("/addView")
    @ResponseBody
    public Result addView(@RequestParam("userId") long userId, @RequestParam("productDetailId") long productDetailId){
        return viewService.addView(userId,productDetailId);
    }

    // 查询该用户的所有历史浏览记录
    @PostMapping("/selectUserView/{userId}")
    @ResponseBody
    public Result selectUserView(@PathVariable long userId){
        return viewService.selectUserViewByUserId(userId);
    }

    // 清除所有浏览记录
    @PostMapping("/removeUserView/{userId}")
    @ResponseBody
    public Result removeUserView(@PathVariable long userId){
        return viewService.removeUserViewByUserId(userId);
    }

}
