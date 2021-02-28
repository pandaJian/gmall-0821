package com.atguigu.gmall.common.exception;

/**
 * @author : panda Jian
 * @date : 2021-02-27 13:57
 * Description
 */
public class OrderException extends RuntimeException{
    public OrderException() {
        super();
    }

    public OrderException(String message) {
        super(message);
    }
}
