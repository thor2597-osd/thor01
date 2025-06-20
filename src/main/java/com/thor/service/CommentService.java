package com.thor.service;

import com.thor.entity.CommentDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.thor.model.Comment;
import com.thor.model.CommentUser;
import com.thor.model.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
public interface CommentService extends IService<CommentDO> {
    /**
     * 发布评论
     * @param refId
     * @param user
     * @param parentId
     * @param content
     * @return
     */
    public Result post(String refId, CommentUser user, long parentId, String content);
    /**
     * 查询评论
     * @param refId
     *  */
    public Result query(String refId);

    /**
     * 删除评论
     * @param comment
     * @param refId
     *  */
    Result delete(Comment comment,String refId);
}
