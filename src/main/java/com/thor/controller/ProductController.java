package com.thor.controller;

import com.thor.entity.ProductDO;
import com.thor.model.Result;
import com.thor.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Controller
@RequestMapping("/api/Product")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 查询所有的商品分类以及里面的商品数量
    @GetMapping("/query")
    @ResponseBody
    public Result queryBrandAndChildrenNumber(){
        return productService.queryBrandAndChildrenNumber();
    }

    // 删除某一商品类
    @PostMapping("/delete")
    @ResponseBody
    public Result deleteProduct(@RequestParam("brand") String brand){
        return productService.delete(brand);
    }

    // 增加商品类
    @PostMapping("/add")
    @ResponseBody
    public Result addProduct(@RequestBody ProductDO productDO){
        return productService.add(productDO);
    }

    // 修改商品类
    @PostMapping("/update")
    @ResponseBody
    public Result updateProduct(@RequestBody ProductDO productDO){
        return productService.updateProduct(productDO);
    }

    @GetMapping("/initCategories")
    @ResponseBody
    public Result initCategories() {
        try {
            List<String> categories = Arrays.asList(
                "手机数码",
                "服装服饰",
                "食品生鲜",
                "美妆护肤",
                "图书文具",
                "运动户外",
                "箱包皮具",
                "玩具乐器",
                "医疗保健",
                "酒水饮料"
            );

            // 批量插入分类
            for (String category : categories) {
                ProductDO product = new ProductDO();
                product.setBrand(category);
                product.setProductDetailNumber(0L);  // 初始商品数量为0
                productService.save(product);
            }

            return Result.success("商品分类初始化成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("商品分类初始化失败：" + e.getMessage());
        }
    }

}
