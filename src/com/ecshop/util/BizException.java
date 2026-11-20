package com.ecshop.util;

/** 业务异常：携带 HTTP 状态码，由 Web 层统一转换为 JSON 响应 */
public class BizException extends RuntimeException {
    public final int status;

    public BizException(int status, String message) {
        super(message);
        this.status = status;
    }
}
