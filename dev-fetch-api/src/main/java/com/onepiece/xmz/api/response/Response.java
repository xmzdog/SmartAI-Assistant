package com.onepiece.xmz.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应返回类
 * 
 * @param <T> 响应数据类型
 * @author SmartAI-Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    /**
     * 成功响应码
     */
    public static final String SUCCESS_CODE = "0000";
    
    /**
     * 失败响应码
     */
    public static final String FAIL_CODE = "9999";
    
    /**
     * 成功消息
     */
    public static final String SUCCESS_MSG = "SUCCESS";

    /**
     * 响应码
     */
    private String code;
    
    /**
     * 响应信息
     */
    private String info;
    
    /**
     * 响应数据
     */
    private T data;

    /**
     * 创建成功响应（带数据）
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(SUCCESS_CODE)
                .info(SUCCESS_MSG)
                .data(data)
                .build();
    }

    /**
     * 创建成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Response<T> success() {
        return success(null);
    }

    /**
     * 创建成功响应（带自定义消息）
     * 
     * @param data 响应数据
     * @param message 自定义消息
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Response<T> success(T data, String message) {
        return Response.<T>builder()
                .code(SUCCESS_CODE)
                .info(message)
                .data(data)
                .build();
    }

    /**
     * 创建失败响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Response<T> fail(String message) {
        return Response.<T>builder()
                .code(FAIL_CODE)
                .info(message)
                .data(null)
                .build();
    }

    /**
     * 创建失败响应（带错误码）
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Response<T> fail(String code, String message) {
        return Response.<T>builder()
                .code(code)
                .info(message)
                .data(null)
                .build();
    }

    /**
     * 判断响应是否成功
     * 
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.code);
    }

    /**
     * 判断响应是否失败
     * 
     * @return true-失败，false-成功
     */
    public boolean isFail() {
        return !isSuccess();
    }

}
