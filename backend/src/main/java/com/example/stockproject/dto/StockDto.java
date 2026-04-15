package com.example.stockproject.dto;

import java.util.List;

public class StockDto {
    private String stockCode;
    private String stockName;
    private String currentPrice;
    private String previousDayDiff;
    private String changeRate;
    private String openPrice;
    private String highPrice;
    private String lowPrice;
    private String volume;
    private String tradingValue;
    private String marketCap;
    private String indexValue;
    private String indexChange;
    private List<StockDto> topGainers;
    private List<StockDto> topLosers;
    private List<StockDto> topMarketCap;
    private List<StockDto> topVolume;

    public StockDto() {}

    public StockDto(String stockCode, String stockName, String currentPrice, String previousDayDiff, String changeRate) {
        this.stockCode = stockCode; //주식 코드
        this.stockName = stockName; //사명
        this.currentPrice = currentPrice; //현재가
        this.previousDayDiff = previousDayDiff; //전일 대비 등락값
        this.changeRate = changeRate; //전일 대비 등락률
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

    public String getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getPreviousDayDiff() {
        return previousDayDiff;
    }

    public void setPreviousDayDiff(String previousDayDiff) {
        this.previousDayDiff = previousDayDiff;
    }

    public String getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(String changeRate) {
        this.changeRate = changeRate;
    }

    public String getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(String openPrice) {
        this.openPrice = openPrice;
    }

    public String getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(String highPrice) {
        this.highPrice = highPrice;
    }

    public String getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(String lowPrice) {
        this.lowPrice = lowPrice;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getTradingValue() {
        return tradingValue;
    }

    public void setTradingValue(String tradingValue) {
        this.tradingValue = tradingValue;
    }

    public String getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(String marketCap) {
        this.marketCap = marketCap;
    }

    public String getIndexValue() {
        return indexValue;
    }

    public void setIndexValue(String indexValue) {
        this.indexValue = indexValue;
    }

    public String getIndexChange() {
        return indexChange;
    }

    public void setIndexChange(String indexChange) {
        this.indexChange = indexChange;
    }

    public List<StockDto> getTopGainers() {
        return topGainers;
    }

    public void setTopGainers(List<StockDto> topGainers) {
        this.topGainers = topGainers;
    }

    public List<StockDto> getTopLosers() {
        return topLosers;
    }

    public void setTopLosers(List<StockDto> topLosers) {
        this.topLosers = topLosers;
    }

    public List<StockDto> getTopMarketCap() {
        return topMarketCap;
    }

    public void setTopMarketCap(List<StockDto> topMarketCap) {
        this.topMarketCap = topMarketCap;
    }

    public List<StockDto> getTopVolume() {
        return topVolume;
    }

    public void setTopVolume(List<StockDto> topVolume) {
        this.topVolume = topVolume;
    }
}