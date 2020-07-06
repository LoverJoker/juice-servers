package com.saas.generator.entity;

import java.util.ArrayList;
import java.util.List;

public class R {

    private int code = 0;
    private String msg = "success";
    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public  static R isOk(){
            return new R();
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public R data(Object data){
        this.data = data;
        return this;
    }


    public R pageData(Object data){
        this.data = new PageInfo(data);
        return this;
    }

    public static class PageInfo {
        public int pageSize = 20;
        public int pageCount = 10;
        public int pageNo = 1;
        public int rowsCount = 200;
        public List<Object> pageData = new ArrayList<>();

        public PageInfo(Object pageData) {
            this.pageData.add(pageData);
        }
    }



}
