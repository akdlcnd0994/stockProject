package com.example.stockproject.repository;

import com.example.stockproject.entity.TimePriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimePriceRepository extends JpaRepository<TimePriceEntity, Long> {

    // 특정 날짜의 데이터 조회
    List<TimePriceEntity> findByTpdate(LocalDate tpdate);

    // 특정 종목의 데이터 조회
    List<TimePriceEntity> findByTpname(String tpname);

    // 특정 날짜와 종목의 데이터 조회
    List<TimePriceEntity> findByTpdateAndTpname(LocalDate tpdate, String tpname);
}
