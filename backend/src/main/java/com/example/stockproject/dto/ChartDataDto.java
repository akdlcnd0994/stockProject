package com.example.stockproject.dto;

import java.util.List;

/**
 * 차트 데이터 응답을 위한 DTO
 * 종목 정보와 캔들 데이터 리스트를 포함
 */
public class ChartDataDto {
    private String stockCode;      // 종목코드
    private String stockName;      // 종목명
    private String timeframe;      // 기간 구분 (D: 일봉, W: 주봉, M: 월봉)
    private List<CandleDto> candles; // 캔들 데이터 리스트

    public ChartDataDto() {}

    public ChartDataDto(String stockCode, String stockName, String timeframe, List<CandleDto> candles) {
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.timeframe = timeframe;
        this.candles = candles;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public List<CandleDto> getCandles() {
        return candles;
    }

    public void setCandles(List<CandleDto> candles) {
        this.candles = candles;
    }
}
