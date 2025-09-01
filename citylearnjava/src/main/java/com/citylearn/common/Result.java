package com.citylearn.common;

import java.util.ArrayList;

public class Result<E> {
    private E data;
    private Integer code=0;

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public  static <E> Result<E> success(E data){
        Result<E> result=new Result<>();
        result.setData(data);
        return result;
    }

    public  static <E> Result<E> success(){
        Result<E> result=new Result<>();
        return result;
    }

    public  static <E> Result<E> fail(Integer code){
        Result<E> result=new Result<>();
        result.setCode(code);
        return result;
    }

    public  static <E> Result<E> fail(){
        Result<E> result=new Result<>();
        result.setCode(500);
        return result;
    }
}
