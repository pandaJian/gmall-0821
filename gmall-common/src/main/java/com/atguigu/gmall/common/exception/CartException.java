package com.atguigu.gmall.common.exception;

/**
 * @author : panda Jian
 * @date : 2021-02-23 20:52
 * Description
 */
public class CartException extends RuntimeException{
    public CartException() {
        super();
    }

    public CartException(String message) {
        super(message);
    }
}
