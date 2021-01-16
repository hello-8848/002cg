package com.changgou.exception;


import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @Auther lxy
 * @Date
 */
@RestControllerAdvice
public class ChanggouExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ChanggouExceptionHandler.class);
/**
 *未知错误异常
 * @param ex :
 * @return : com.changgou.util.Result
 */
    @ExceptionHandler(value = Exception.class)
    public Result handleException(Exception ex) {
        log.error("发生系统异常", ex);
        return new Result(false, StatusCode.EXCEPTION,"服务器异常", ex.getMessage());
    }
/**
 *程序执行异常
 * @param ce :
 * @return : com.changgou.util.Result
 */
    @ExceptionHandler(value = ChanggouException.class)
    public Result handleChanggouException(ChanggouException ce) {
        log.error("程序执行异常", ce);
        return new Result(false, StatusCode.ERROR,"服务器异常", ce.getMessage());
    }
}
