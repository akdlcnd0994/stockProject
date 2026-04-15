package com.example.stockproject.dto;

/**
 * 캔들 차트 데이터를 담는 DTO
 * OHLCV (Open, High, Low, Close, Volume) 데이터 포함
 */
public class CandleDto {
    private String date;           // 날짜 (YYYYMMDD)
    private String open;           // 시가
    private String high;           // 고가
    private String low;            // 저가
    private String close;          // 종가
    private String volume;         // 거래량
    private String changeRate;     // 등락률

    public CandleDto() {}

    public CandleDto(String date, String open, String high, String low, String close, String volume, String changeRate) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.changeRate = changeRate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(String changeRate) {
        this.changeRate = changeRate;
    }
}
