package com.suimz.example.api.signature.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 接口响应封装对象
 * @param <T>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = -1L;

    private boolean success;

    private String errorCode;

    private String errorDesc;

    private T data;

    public ApiResult() {

    }

    public static <T> ApiResult<T> fail(String code, String desc) {
        ApiResult<T> result = new ApiResult<>();
        result.setSuccess(false);
        result.setErrorCode(code);
        result.setErrorDesc(desc);
        return result;
    }

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    public static ApiResult success() {
        ApiResult result = new ApiResult<>();
        result.setSuccess(true);
        return result;
    }
}
