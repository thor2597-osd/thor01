package com.secondhand.trading.controller;

import cn.hutool.json.JSONUtil;
import com.secondhand.trading.model.ProductDetail;
import com.secondhand.trading.model.Result;
import com.secondhand.trading.service.ListingService;
import com.secondhand.trading.service.OssService;
import com.secondhand.trading.service.ProductDetailService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LJX
 * @since 2024-11-01
 */
@Controller
@RequestMapping("/api/Listing")
@AllArgsConstructor
public class ListingController {

    private final ListingService listingService;

    private final OssService ossService;

    private final ProductDetailService productDetailService;
    //发布添加
    @PostMapping("/listingAdd")
    @ResponseBody
    public Result add(@RequestParam("file") List<MultipartFile> files,
                      @RequestParam("productDetail") String productDetail)
    {
        try {
            // 获取图片文件路径
            List<String> filePaths = new ArrayList<>();
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file:files) {
                    String photoPath = ossService.uploadPhoto(file);
                    filePaths.add(photoPath);
                }
            }
            // 初始化文件详情并将其存储
            ProductDetail detail = JSONUtil.toBean(productDetail, ProductDetail.class);
            // 存储到缓存和数据库并且返回主键
            long productDetailId = productDetailService.initAndSave(detail, filePaths);
            // 存储发布商品服务
            return listingService.save(productDetailId,filePaths);
        }catch (Exception e){
            e.printStackTrace();
            return Result.fail("发布失败");
        }

    }

    //查（单个用户已发布的商品）
    @PostMapping("/list")
    @ResponseBody
    public Result list(@RequestParam("userId")long userId,@RequestParam(value = "brand",required = false)String brand){
        return listingService.selectByUserId(userId,brand);
    }

}
