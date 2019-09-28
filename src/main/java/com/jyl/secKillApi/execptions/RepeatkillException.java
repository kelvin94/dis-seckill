package com.jyl.secKillApi.execptions;

public class RepeatkillException extends SeckillException{

    public RepeatkillException(String msg) {
        super(msg);
    }

    public RepeatkillException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
