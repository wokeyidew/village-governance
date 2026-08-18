package com.scau.village.common.result;

/**
 * 统一响应结果封装类
 * 所有接口返回的统一格式
 *
 * @param <T> 数据类型
 * @author system
 * @since 2026-08-15
 */
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return Result对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功响应（带自定义消息和数据）
     *
     * @param message 自定义消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return Result对象
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 错误响应（带自定义状态码和消息）
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 错误响应（默认500状态码）
     *
     * @param message 错误信息
     * @param <T>     数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 错误响应（带自定义状态码、消息和数据）
     *
     * @param code    错误码
     * @param message 错误信息
     * @param data    附加数据
     * @param <T>     数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    // ==================== 便捷判断方法 ====================

    /**
     * 判断是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    /**
     * 判断是否失败
     *
     * @return true-失败，false-成功
     */
    public boolean isError() {
        return !isSuccess();
    }

    // ==================== Getter & Setter ====================

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}