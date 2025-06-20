package com.secondhand.trading.controller;

import com.secondhand.trading.model.Result;
import com.secondhand.trading.service.CollectionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LJX
 * @since 2024-11-01
 */
@RestController
@RequestMapping("/api/Collection")
@AllArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    // 将某一商品添加至收藏
    @PostMapping("/addToCollection")
    @ResponseBody
    public Result addToCollection(@RequestParam("userId") long userId,
                                  @RequestParam("productDetailId") long productDetailId) {
        return collectionService.addToCollection(userId,productDetailId);
    }
    // 将某一个商品移除收藏
    @PostMapping("/removeFromCollection")
    @ResponseBody
    public Result removeFromCollection(@RequestParam("userId") long userId,
                                       @RequestParam("productDetailId") long productDetailId) {
        return collectionService.removeFromCollection(userId,productDetailId);
    }
    // 查看单个用户的收藏
    @GetMapping("/selectCollectionById/{userId}")
    @ResponseBody
    public Result selectCollectionById(@PathVariable Long userId){
        return collectionService.selectCollectionById(userId);
    }

}