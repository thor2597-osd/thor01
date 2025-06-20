package com.thor.controller;

import com.thor.model.Comment;
import com.thor.model.CommentUser;
import com.thor.model.Result;
import com.thor.service.CommentService;
import com.thor.service.ProductDetailService;
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
@RequestMapping("/api/Comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final ProductDetailService productDetailService;

    @PostMapping("/publish")
    @ResponseBody
    public Result post(@RequestParam("refId") String refId,
                       @RequestBody CommentUser user,
                       @RequestParam("parentId") long parentId,
                       @RequestParam("content") String content) {
        // 更改评论数量
        productDetailService.updateCommentToProductDetail(Long.parseLong(refId),1,0);
        // 发表评论
        return commentService.post(refId,user,parentId,content);
    }

    @PostMapping("/query")
    @ResponseBody
    public Result query(@RequestParam("refId") String refId) {
        // 查询评论
        return commentService.query(refId);
    }

    @PostMapping("/delete")
    @ResponseBody
    public Result delete(@RequestBody Comment comment, @RequestParam("productDetailId")String refId) {
        // 删除单个评论
        return commentService.delete(comment,refId);
    }
}
