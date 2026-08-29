package com.scau.village.common.exception;

import com.scau.village.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 * 
 * 修复说明（2026-08-30）：
 * - BusinessException 根据消息内容自动映射 HTTP 状态码
 * - 包含“不存在”、“未找到”等关键词时返回 404
 * - 包含“无权”、“权限”等关键词时返回 403
 * - 其他业务异常默认返回 400（参数错误）或 500（服务器错误）
 *
 * @author system
 * @since 2026-07-17
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：根据 code 和消息内容映射 HTTP 状态码
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        
        // 根据业务 code 映射 HTTP 状态码
        HttpStatus status;
        int code = e.getCode();
        
        // 如果 code 已经明确设置，优先使用 code 映射
        if (code == 401) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (code == 403) {
            status = HttpStatus.FORBIDDEN;
        } else if (code == 400) {
            status = HttpStatus.BAD_REQUEST;
        } else if (code == 404) {
            status = HttpStatus.NOT_FOUND;
        } else {
            // code 未明确指定，根据消息内容智能判断
            String msg = e.getMessage();
            if (msg != null) {
                if (msg.contains("不存在") || msg.contains("未找到") || msg.contains("已删除") || msg.contains("已失效")) {
                    status = HttpStatus.NOT_FOUND;
                } else if (msg.contains("无权") || msg.contains("权限") || msg.contains("禁止")) {
                    status = HttpStatus.FORBIDDEN;
                } else if (msg.contains("参数") || msg.contains("格式") || msg.contains("必须")) {
                    status = HttpStatus.BAD_REQUEST;
                } else {
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                }
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        
        Result<?> result = Result.error(e.getCode(), e.getMessage());
        return new ResponseEntity<>(result, status);
    }

    /**
     * 权限不足（Spring Security 抛出）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<?>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        Result<?> result = Result.error(403, "权限不足，请联系管理员");
        return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
    }

    /**
     * 参数校验异常（@Valid 校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        Result<?> result = Result.error(400, msg);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<?>> handleBindException(BindException e) {
        String msg = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("参数绑定失败");
        log.warn("参数绑定失败: {}", msg);
        Result<?> result = Result.error(400, msg);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    /**
     * 参数校验异常（@RequestParam 等）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<?>> handleConstraintViolationException(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        Result<?> result = Result.error(400, msg);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    /**
     * 文件上传大小超限异常（multipart 文件大于 max-file-size）
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<?>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件上传大小超限: {}", e.getMessage());
        Result<?> result = Result.error(400, "文件大小超过限制（最大 10MB），请压缩后重试");
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    /**
     * 文件上传异常（multipart 解析失败）
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Result<?>> handleMultipartException(MultipartException e) {
        log.warn("文件上传异常: {}", e.getMessage());
        Result<?> result = Result.error(400, "文件上传失败，请检查文件格式是否正确");
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    /**
     * 404 路径未找到异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<?>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        Result<?> result = Result.error(404, "接口不存在");
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    /**
     * 其他未捕获异常（统一返回 500，并打印完整堆栈）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        log.error("系统异常: ", e);  // ✅ 打印完整堆栈，方便定位
        Result<?> result = Result.error(500, "系统繁忙，请稍后再试");
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}