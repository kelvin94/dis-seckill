package com.jyl.secKillApi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
public class SeckillOrder implements Serializable {
    @Id
    private long seckillSwagId;
    private BigDecimal total; //支付金额

    private long userPhone; //秒杀用户的手机号

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime; //创建时间

    private boolean state; //订单状态， -1:无效 0:成功 1:已付款

    private SeckillSwag secKillSwag; //秒杀商品，和订单是一对多的关系
}
