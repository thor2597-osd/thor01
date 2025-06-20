package com.thor.model;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class Paging<R> implements Serializable {
    //页数
    private int pageNum = 1;
    //每页数量
    private int pageSize = 10;
    //总页数
    private long pageTotal;
    //总记录数
    private long pageTotalNum;
    //集合数
    private List<R> data;

    public Paging() {
    }

    public Paging(int pageNum, int pageSize, long pageTotal, long pageTotalNum, List<R> data) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pageTotal = pageTotal;
        this.pageTotalNum = pageTotalNum;
        this.data = data;
    }
}
