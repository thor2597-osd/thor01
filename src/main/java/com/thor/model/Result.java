package com.thor.model;

import lombok.Data;

//把服务器的后端结果再进行封装
@Data
public class Result {
    //1.返回的编码，200成功，400失败
    private int code;
    //2.success还是fail
    private String message;
    //3.返回的总记录数
    private long total;
    //4.返回的数据
    private Object data;

    public static Result fail(){
        return result(400,"失败",0L,null);
    }
    public static Result fail(String message){
        return result(400,message,0L,null);
    }
    public static Result fail(Object data){
        return result(400,"失败",0L,data);
    }
    public static Result success(long total,Object data){
        return result(200,"成功",total,data);
    }
    public static Result success(String message,Object data){
        return result(200,message,1L,data);
    }
    public static Result success(String message,long total,Object data){
        return result(200,message,total,data);
    }
    public static Result success(){
        return result(200,"成功",0L,null);
    }

    public static Result success(Object data){
        return result(200,"成功",1L,data);
    }

    private static Result result(int code, String message, long total, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        result.setTotal(total);
        return result;
    }
}
