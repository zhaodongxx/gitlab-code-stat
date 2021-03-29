package com.hikcreate.com.gitlab.code.stat.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhaodongxx
 * @date 2020/12/21 17:48
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class Response<T> {

    /**
     * 成功标记
     */
    private boolean success;

    /**
     * 状态码
     */
    private long code;

    /**
     * 描述
     */
    private String msg;

    /**
     * 挂载数据
     */
    private T data;

    public Response() {
        this.success = true;
        this.code = 1000;
        this.msg = "操作成功";
    }

    public Response<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public Response<T> setData(T data) {
        this.data = data;
        return this;
    }
}
