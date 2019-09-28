package com.jyl.secKillApi.dto;

import lombok.Data;

@Data
public class UrlExposer {

    private Boolean isExposed; // 是否开启秒杀

    // exposer = 暴露接口用到的方法，目的就是获取秒杀商品抢购的地址
    private String md5Url;

    private Long seckillSwagId;

    private Long dealStart; // 秒杀时间开始

    private Long dealEnd;

    private Long now; // current utc time

    public UrlExposer(boolean isExposed, String md5Url, long seckillSwagId) {
        this.isExposed = isExposed;
        this.md5Url = md5Url;
        this.seckillSwagId = seckillSwagId;
    }

    public UrlExposer(boolean isExposed, Long seckillSwagId, long now, long dealStart, long dealEnd) {
        this.isExposed = isExposed;
        this.seckillSwagId = seckillSwagId;
        this.now = now;
        this.dealStart = dealStart;
        this.dealEnd = dealEnd;
    }

    public UrlExposer(boolean isExposed, long seckillSwagId) {
        this.isExposed = isExposed;
        this.seckillSwagId = seckillSwagId;
    }
}
