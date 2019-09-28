package com.jyl.secKillApi.execptions;

public class SeckillException extends RuntimeException{
    //系统还可能出现其他位置异常，所以我们还需要定义一个异常继承所有异常的父类Exception:
    public SeckillException(String msg) {
        //针对秒杀关闭的异常
        super(msg);
    }

    public SeckillException(String msg, Throwable cause) {
        super(msg);
    }
}
