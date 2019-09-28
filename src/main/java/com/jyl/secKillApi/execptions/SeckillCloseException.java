package com.jyl.secKillApi.execptions;

public class SeckillCloseException extends SeckillException{
//    针对秒杀关闭的异常: seckill 过期了
    public SeckillCloseException(String msg) {
        super(msg);
    }

    public SeckillCloseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SeckillCloseException(String msg, Throwable cause, Long seckillSwagId, Long end_time) {
        super(msg, cause);
    }
}
