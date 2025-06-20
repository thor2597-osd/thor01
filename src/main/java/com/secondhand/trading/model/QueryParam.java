package com.secondhand.trading.model;

import lombok.Data;
import java.util.HashMap;

/*
 * 分页封装
 * */
@Data
public class QueryParam {
    //当前页码默认值，默认为第一页
    private int pageNum = 1;
    //每页数据量，这边默认每页10条数据。
    private int pageSize = 10;
    //封装分页参数
    private HashMap<Object,Object> param = new HashMap<>();
}
