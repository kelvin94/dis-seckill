package com.jyl.secKillApi.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMsgBody {
    private Calendar msgId;
    private Long seckillSwagId;
    private Long userPhone;
}
