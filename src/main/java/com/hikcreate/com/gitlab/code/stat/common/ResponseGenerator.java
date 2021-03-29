package com.hikcreate.com.gitlab.code.stat.common;

/**
 * @author zhaodongxx
 * @date 2020/12/21 17:48
 */
public class ResponseGenerator {

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setData(data);
        return response;
    }

    public static <T> Response<T> success4Msg(String msg) {
        Response<T> response = new Response<>();
        response.setMsg(msg);
        return response;
    }
}
