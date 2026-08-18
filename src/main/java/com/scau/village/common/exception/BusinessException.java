package com.scau.village.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于封装业务逻辑中出现的可预知异常，包含错误码和错误信息
 *
 * @author system
 * @since 2026-07-17
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;

    /**
     * 构造业务异常（指定错误码和消息）
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造业务异常（默认错误码为 500）
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        this(500, message);
    }

    /**
     * 构造业务异常（指定错误码、消息和原因）
     *
     * @param code    错误码
     * @param message 错误信息
     * @param cause   原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 构造业务异常（默认错误码 500，包含原因）
     *
     * @param message 错误信息
     * @param cause   原因
     */
    public BusinessException(String message, Throwable cause) {
        this(500, message, cause);
    }
}