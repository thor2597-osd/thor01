package com.secondhand.trading.model;

import lombok.Data;

@Data
public class CommentUser {
    private long id;

    /**
     * 用户的昵称
     */
    private String nickName;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户头像
     */
    private String avatar;
}
