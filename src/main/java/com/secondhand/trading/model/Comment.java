package com.secondhand.trading.model;

import com.secondhand.trading.entity.CommentDO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/*
* 评论
* */
@Data
public class Comment {
    private long id;
    //关联用户发布商品（productDetail）的id
    private String refId;
    //作者（写评论的人）
    private CommentUser author;
    //内容
    private String content;
    //子评论
    private List<Comment> children;
    //父类内容Id
    private long parentId;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreated;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtModified;

    public CommentDO toConvert(){
        CommentDO commentDO = new CommentDO();
        commentDO.setId(this.getId());
        commentDO.setParentId(this.getParentId());
        commentDO.setContent(this.getContent());
        commentDO.setGmtModified(this.getGmtModified());
        commentDO.setGmtCreated(this.getGmtCreated());
        commentDO.setUserId(this.getAuthor().getId());
        commentDO.setRefId(this.getRefId());
        return commentDO;
    }
}
