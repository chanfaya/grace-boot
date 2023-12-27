package com.grace.common.exception;

import java.io.Serial;

/**
 * 自定义异常
 *
 * @author chanfa
 */
public class CustomException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5198969658351291908L;

    private final String message;

    public CustomException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
