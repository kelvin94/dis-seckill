package com.jyl.secKillApi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UrlExposer implements Serializable {

    private Boolean isExposed; // 是否开启秒杀

    // exposer = 暴露接口用到的方法，目的就是获取秒杀商品抢购的地址
    private String md5Url;

    private Long seckillSwagId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long dealStart; // 秒杀时间开始
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long dealEnd;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long now; // current utc time
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int stockCount;
    private BigDecimal seckill_price; //商品秒杀价格


    public UrlExposer(boolean isExposed, String md5Url, long seckillSwagId, int stockCount, long dealStart,
                      long dealEnd, BigDecimal seckill_price) {
        this.isExposed = isExposed;
        this.md5Url = md5Url;
        this.seckillSwagId = seckillSwagId;
        this.stockCount = stockCount;
        this.dealStart = dealStart;
        this.dealEnd = dealEnd;
        this.seckill_price = seckill_price;
    }

    public UrlExposer(boolean isExposed, Long seckillSwagId, long now, long dealStart, long dealEnd, int stockCount) {
        this.isExposed = isExposed;
        this.seckillSwagId = seckillSwagId;
        this.now = now;
        this.dealStart = dealStart;
        this.dealEnd = dealEnd;
        this.stockCount = stockCount;
    }

    public UrlExposer(boolean isExposed, long seckillSwagId) {
        this.isExposed = isExposed;
        this.seckillSwagId = seckillSwagId;
    }

    @Override
    public String toString() {
        return "UrlExposer{" +
                "isExposed=" + isExposed +
                ", md5Url='" + md5Url + '\'' +
                ", seckillSwagId=" + seckillSwagId +
                ", dealStart=" + dealStart +
                ", dealEnd=" + dealEnd +
                ", now=" + now +
                '}';
    }
}
