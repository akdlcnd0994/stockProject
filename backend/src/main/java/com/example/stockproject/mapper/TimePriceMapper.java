package com.example.stockproject.mapper;

import com.example.stockproject.dto.TimePriceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TimePriceMapper {

    /**
     * 가격 데이터 일괄 저장
     */
    void insertTimePriceList(@Param("list") List<TimePriceDto> list);

    /**
     * 단일 가격 데이터 저장
     */
    void insertTimePrice(TimePriceDto timePriceDto);

    /**
     * 특정 날짜의 데이터 조회
     */
    List<TimePriceDto> selectByTpdate(@Param("tpdate") LocalDate tpdate);

    /**
     * 특정 종목의 데이터 조회
     */
    List<TimePriceDto> selectByTpname(@Param("tpname") String tpname);

    /**
     * 특정 날짜와 종목의 데이터 조회
     */
    List<TimePriceDto> selectByTpdateAndTpname(@Param("tpdate") LocalDate tpdate, @Param("tpname") String tpname);

    /**
     * 전체 데이터 조회
     */
    List<TimePriceDto> selectAll();
}
