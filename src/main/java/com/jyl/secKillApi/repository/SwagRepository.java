package com.jyl.secKillApi.repository;

import com.jyl.secKillApi.entity.SeckillSwag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwagRepository extends JpaRepository<SeckillSwag, Long> {
    // JpaRepository provides basic CRUD implementation
    public List<SeckillSwag> findAll();
}
