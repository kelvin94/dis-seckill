package com.jyl.secKillApi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class SeckillOrder implements Serializable {
//    @Id
//    private long seckillSwagId;
    @EmbeddedId
    private SeckillOrderPrimaryKey orderId;

    private BigDecimal total; //支付金额

//    private long userPhone; //秒杀用户的手机号

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time", insertable = false) // "insertable" = false, get the column out of your SQL INSERTs so that dbms will set "create_time" to the default value we set in the schema.
    private Date createTime; //创建时间

    private int state; //订单状态， -1:无效 0:成功 1:已付款
    @Transient
    private SeckillSwag secKillSwag; //秒杀商品，和订单是一对多的关系

    public SeckillOrder(Long seckillSwagId, BigDecimal seckill_price, Long userPhone, int state) {
        SeckillOrderPrimaryKey temp = new SeckillOrderPrimaryKey();
        temp.setSeckillSwagId(seckillSwagId);
        temp.setUserPhone(userPhone);
        this.orderId = temp;
        this.state = state;
        this.total = seckill_price;
    }
}
