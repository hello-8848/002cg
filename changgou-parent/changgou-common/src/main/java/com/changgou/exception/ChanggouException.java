package com.changgou.exception;

/**
 * @Auther lxy
 * @Date
 */
//自定义异常区分系统异常与业务异常
public class ChanggouException extends RuntimeException {
    public ChanggouException(String message) {
        super(message);

    }
}
