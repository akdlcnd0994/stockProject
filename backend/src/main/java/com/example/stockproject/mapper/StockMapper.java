package com.example.stockproject.mapper;

import com.example.stockproject.dto.StockDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockMapper {

    StockDto selectAllUsers();

    StockDto selectUserById(@Param("id") Long id);


    //int deleteUser(@Param("id") Long id);

}