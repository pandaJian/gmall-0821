package com.atguigu.gmall.common.exception;

/**
 * @author : panda Jian
 * @date : 2021-02-22 18:44
 * Description
 */
public class UserException extends RuntimeException{
    public UserException() {
        super();
    }

    public UserException(String message) {
        super(message);
    }
}
