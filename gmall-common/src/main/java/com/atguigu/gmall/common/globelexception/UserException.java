package com.atguigu.gmall.common.globelexception;

public class UserException extends Exception {
    public UserException() {
    }

    public UserException(String msg){
        super(msg);
    }
}
