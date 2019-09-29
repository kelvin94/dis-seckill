package com.jyl.secKillApi.service;

import com.jyl.secKillApi.dto.SeckillExecution;
import com.jyl.secKillApi.dto.UrlExposer;
import com.jyl.secKillApi.entity.SeckillOrder;
import com.jyl.secKillApi.entity.SeckillOrderPrimaryKey;
import com.jyl.secKillApi.entity.SeckillSwag;
import com.jyl.secKillApi.execptions.RepeatkillException;
import com.jyl.secKillApi.execptions.SeckillCloseException;
import com.jyl.secKillApi.execptions.SeckillException;
import com.jyl.secKillApi.repository.OrderRepository;
import com.jyl.secKillApi.repository.SwagRepository;
import com.jyl.secKillApi.resource.SeckillParameter;
import com.jyl.secKillApi.stateenum.SeckillStateEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SeckillServiceImpl implements SeckillService {
    private static Logger logger = LogManager.getLogger(SeckillServiceImpl.class.getSimpleName());
    private final SwagRepository swagRepository;
    private final OrderRepository orderRepository;
    private final String salt = "randomsalt555";
    public SeckillServiceImpl(
            SwagRepository swagRepository,
            OrderRepository orderRepository
    ) {
        this.swagRepository = swagRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<SeckillSwag> findAll() {
        return swagRepository.findAll();
    }

    @Override
    public SeckillSwag findBySeckillSwagId(Long seckillSwag_Id) {
        Optional<SeckillSwag> result = swagRepository.findBySeckillSwagId(seckillSwag_Id);
        return result.orElse(null);
    }

    @Override
    public UrlExposer exportSeckillUrl(Long seckillSwagId) {
        Optional<SeckillSwag> swag = swagRepository.findBySeckillSwagId(seckillSwagId);
        if(swag.isPresent()) {
            //generate md5SwagId
           String md5SwagId = getMd5(swag.get().getSeckillSwagId());
           logger.debug("# md5 url " + md5SwagId);
           Date startTs = swag.get().getStartTime();
            Date endTs =  swag.get().getEndTime();
            Date now = new Date();
            long stock = swag.get().getStockCount();
            if(stock > 0 && now.getTime() > startTs.getTime() && now.getTime() < endTs.getTime()) {
                return new UrlExposer(true, md5SwagId, swag.get().getSeckillSwagId());
            }
            return new UrlExposer(false, swag.get().getSeckillSwagId());
        }
        return null;
    }

    private String getMd5(long seckillSwagId) {
        String base = seckillSwagId + "/" + seckillSwagId;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // roll back if any exception is thrown
    public SeckillExecution executeSeckill(SeckillParameter requestParam) throws Exception {
        Long seckillSwagId = requestParam.getSeckillSwagId();
        BigDecimal dealPrice = requestParam.getDealPrice();
        Long userPhone = requestParam.getUserPhone();
        String md5Url = requestParam.getMd5Url();

        if (md5Url == null || !md5Url.equalsIgnoreCase(getMd5(seckillSwagId))) {
            throw new SeckillException("seckill data is tampered. hashed result is different.");
        }

        try {
            Optional<SeckillSwag> swag = swagRepository.findBySeckillSwagId(seckillSwagId);

            Date currentSysTime = new Date();
            if(swag.isPresent() && currentSysTime.compareTo(swag.get().getStartTime()) > 0 && currentSysTime.compareTo(swag.get().getEndTime()) < 0 ) {
                logger.debug("# get into the if statement (should NOT see this)");
                long remainingStockCount = swag.get().getStockCount();
                logger.debug("# 剩余库存 " + remainingStockCount);
                if (remainingStockCount <= 0) {
                    throw new SeckillCloseException("卖完啦洗洗睡吧.");
                } else {
                    // 减库存
                    remainingStockCount -= 1;
                    int updatedRows = swagRepository.updateStockCount(remainingStockCount, seckillSwagId);
                    logger.debug("#完成更新库存");
                    if (updatedRows != 1) {
                        throw new Exception("Somethings wrong...Updated more than 1 swag's stock_count.");
                    }
                    int state = 1;
                    try {
                        orderRepository.insertOder(swag.get().getSeckillSwagId(), swag.get().getSeckill_price(), userPhone, state);
                        logger.debug("######finish insertion");
                    }  catch( DataIntegrityViolationException ex) {
                        logger.debug("######get into DataIntegrityViolationException");
                        throw new RepeatkillException(Objects.requireNonNull(SeckillStateEnum.stateOf(-1)).getStateInfo());
                    }

                    return new SeckillExecution(swag.get().getSeckillSwagId(), 1, Objects.requireNonNull(SeckillStateEnum.stateOf(1)).getStateInfo());

                    //                    SeckillOrder order = new SeckillOrder();
                    //                    SeckillOrderPrimaryKey orderId = new SeckillOrderPrimaryKey();
                    //                    orderId.setSeckillSwagId(swag.get().getSeckillSwagId());
                    //                    orderId.setUserPhone(userPhone);
                    //                    order.setOrderId(orderId);
                    ////                    order.setSeckillSwagId(swag.get().getSeckillSwagId());
                    //                    order.setTotal(swag.get().getSeckill_price());
                    ////                    order.setUserPhone(userPhone);
                    //                    order.setState(state);
                    //                    SeckillOrder returnedInstance = orderRepository.save(order);

                }

            }

        } catch (RepeatkillException ex) {
            logger.error("userPhone " + userPhone + " try to buy twice this swag id: " + seckillSwagId);
            throw new RepeatkillException("userPhone " + userPhone + " try to buy twice this swag id: " + seckillSwagId);
        } catch (SeckillCloseException ex) {
            throw new SeckillCloseException("user phone " + userPhone);
        } catch (SeckillException ex) {
            // all other exceptions...
            logger.error("##userPhone: " + userPhone + " " + ex.getMessage());
            throw new SeckillException(ex.getMessage());
        }
        logger.debug("# return null (should see this)");
        return null;
    }
}
