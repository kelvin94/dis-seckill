package com.jyl.secKillApi.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
@Data
public class SeckillOrderPrimaryKey implements Serializable {
    @NotNull
    private long seckillSwagId;
    @NotNull
    private long userPhone; //秒杀用户的手机号

}
