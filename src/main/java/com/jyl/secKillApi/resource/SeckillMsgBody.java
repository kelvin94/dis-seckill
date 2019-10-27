package com.jyl.secKillApi.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMsgBody {
    private Long seckillSwagId;
    private Long userPhone;
}
