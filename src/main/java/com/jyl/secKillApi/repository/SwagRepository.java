package com.jyl.secKillApi.repository;

import com.jyl.secKillApi.entity.SeckillSwag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwagRepository extends JpaRepository<SeckillSwag, Long> {

    // JpaRepository provides basic CRUD implementation
    public List<SeckillSwag> findAll();

    public Optional<SeckillSwag> findBySeckillSwagId(Long seckillSwagId);

    @Query(value = "select stock_count from seckill_swag where seckill_swag_id = ?1", nativeQuery = true)
    public Long findRemainingStock(Long seckillSwagId);

    @Modifying
    @Query(value = "update seckill_swag set stock_count = ?1 where seckill_swag_id = ?2", nativeQuery = true)
    public int updateStockCount(int newSockCount, Long seckillSwagId);
}
