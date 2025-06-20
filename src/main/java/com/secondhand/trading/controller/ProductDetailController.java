package com.secondhand.trading.controller;

import com.secondhand.trading.entity.CartDO;
import com.secondhand.trading.model.Cart;
import com.secondhand.trading.model.ProductDetail;
import com.secondhand.trading.model.QueryParam;
import com.secondhand.trading.model.Result;
import com.secondhand.trading.service.CommentService;
import com.secondhand.trading.service.ListingService;
import com.secondhand.trading.service.ProductDetailService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Controller
@RequestMapping("/api/ProductDetail")
@AllArgsConstructor
public class ProductDetailController {

    private final ProductDetailService productDetailService;

    private final CommentService commentService;

    private final ListingService listingService;

    //分页查询出所有信息
    @PostMapping("/listPage")
    @ResponseBody
    public Result listPage(@RequestBody QueryParam query){
        return productDetailService.pagination(query);
    }

    // 点击任一商品后进入页面查看商品详情
    @PostMapping("/getProductDetails")
    @ResponseBody
    public Result getProductDetails(@RequestParam("id")int id){
        Result result = new Result();
        if (id <= 0) {
            result.setCode(400);
            result.setMessage("商品不存在");

            return result;
        }
        // 先获得商品详情
        ProductDetail productDetail = productDetailService.getProductDetailsById(id);
        if (productDetail == null) {
            result.setCode(400);
            result.setMessage("商品不存在");

            return result;
        }
        // 再获取商品的相关评论
        Result query = commentService.query(String.valueOf(id));
        // 存储在map里面统一返回
        Map<String,Object> map = new HashMap<>();
        map.put("productDetail",productDetail);
        map.put("comment",query.getData());

        result.setCode(200);
        result.setMessage("查询成功");
        result.setTotal(1);
        result.setData(map);

        return result;
    }

    // 搜索框查询
    @PostMapping("/selectByName")
    @ResponseBody
    public Result selectByName(@RequestParam("name") String name) {
        return productDetailService.selectByName(name);
    }

    // 删除（下架）商品
    @PostMapping("/delete")
    @ResponseBody
    public Result deleteProduct(@RequestParam("userId")long userId,@RequestParam("productDetailId")long productDetailId){
        // 同时，发布商品里面的商品详情Id也要删除
        listingService.delete(userId,productDetailId);
        return productDetailService.delete(userId, productDetailId);
    }

    // 清空购物车
    @PostMapping("/purchase")
    @ResponseBody
    public Result purchaseProduct(@RequestParam("userId") long userId, @RequestBody List<CartDO> carts){
        return productDetailService.purchase(userId,carts);
    }
}
