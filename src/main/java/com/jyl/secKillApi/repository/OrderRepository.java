package com.jyl.secKillApi.repository;

import com.jyl.secKillApi.entity.SeckillOrder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface OrderRepository extends JpaRepository<SeckillOrder, Long> {
    @Modifying
    @Query(value = "insert into seckill_order(seckill_swag_id, total, user_phone, state) values(:seckillSwagId, :dealPrice, :phoneNumber, :state)", nativeQuery = true)
    public void insertOder(@Param("seckillSwagId") Long seckillSwagId, @Param("dealPrice") BigDecimal dealPrice, @Param("phoneNumber")Long phoneNumber, @Param("state") int state) throws DataIntegrityViolationException;
}
