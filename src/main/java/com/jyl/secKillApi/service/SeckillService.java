package com.jyl.secKillApi.service;

import com.jyl.secKillApi.dto.SeckillExecution;
import com.jyl.secKillApi.dto.UrlExposer;
import com.jyl.secKillApi.entity.SeckillSwag;
import com.jyl.secKillApi.resource.SeckillParameter;

import java.util.List;

public interface SeckillService {

    /**
     * 获取所有的秒杀商品列表
     *
     * @return
     */
    List<SeckillSwag> findAll();

    /**
     * 获取某一条商品秒杀信息
     *
     * @param seckillSwag_Id
     * @return
     */
    SeckillSwag findBySeckillSwagId(Long seckillSwag_Id);

    /**
     * 秒杀开始时输出暴露秒杀的地址
     * 否者输出系统时间和秒杀时间
     *
     * // exposer = 暴露接口用到的方法，目的就是获取秒杀商品抢购的地址
     * @param seckillId
     */
    UrlExposer exportSeckillUrl(Long seckillId);

        /**
         * 执行秒杀的操作
         *
         * @param requestParam
         */
    SeckillExecution executeSeckill(SeckillParameter requestParam)
            throws Exception;
}
