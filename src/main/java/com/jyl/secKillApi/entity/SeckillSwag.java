package com.jyl.secKillApi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Data
@NoArgsConstructor
@Entity
//@Table(
//        name = "seckill_swag",
//        indexes = {
//                @Index(
//                        name = "idx_start_time",
//                        columnList = "start_time",
//                        unique = false
//                ),
//                @Index(
//                        name = "idx_end_time",
//                        columnList = "end_time",
//                        unique = false
//                )
//        }
//)
public class SeckillSwag implements Serializable {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // @GeneratedValue annotation is to configure the way of increment of the specified column(field).
    private long seckillSwagId; //商品ID
    private String title; //商品标题
    private BigDecimal price; //商品原价格
    private BigDecimal seckill_price; //商品秒杀价格


    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT-8")
    private Date createTime; //创建时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT-8")
    private Date startTime; //秒杀开始时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT-8")
    private Date endTime; //秒杀结束时间

    private int stockCount; //剩余库存数量
}
