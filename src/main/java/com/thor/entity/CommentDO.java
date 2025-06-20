package com.thor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.thor.model.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Data
@TableName("tb_comment")
public class CommentDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String refId;

    private Long userId;

    private String content;

    private Long parentId;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreated;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtModified;

    public Comment toModel(){
        Comment comment = new Comment();
        comment.setId(getId());
        comment.setRefId(getRefId());
        comment.setContent(getContent());
        comment.setGmtCreated(getGmtCreated());
        comment.setGmtModified(getGmtModified());
        comment.setParentId(getParentId());
        return comment;
    }
}
