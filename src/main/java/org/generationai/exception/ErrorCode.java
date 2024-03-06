package org.generationai.exception;

public enum ErrorCode {
    SUCCESS(200, "success"),
    SERVER_ERROR(400, "unknown error"),
    VALIDATION_ERROR(500, "unknown error");

    private final Integer code;
    private final String msg;

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    ErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
