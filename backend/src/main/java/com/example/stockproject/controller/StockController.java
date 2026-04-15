package com.example.stockproject.controller;

import com.example.stockproject.dto.ChartDataDto;
import com.example.stockproject.dto.StockDto;
import com.example.stockproject.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = "*")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/indices")
    public ResponseEntity<List<StockDto>> getAllIndices() {
        List<StockDto> indices = new ArrayList<>();

        //백엔드 응답안할 때 더미데이터

        // KOSPI 지수
        StockDto stockDto = stockService.getKospiIndex();
        if (stockDto == null) {
            stockDto = new StockDto();
            stockDto.setStockName("KOSPI");
            stockDto.setIndexValue("2650.50");
            stockDto.setIndexChange("+1.2%");
            stockDto.setChangeRate("0.05");
        }
        indices.add(stockDto);

        // KOSDAQ 지수
        stockDto = stockService.getKosdaqIndex();
        if (stockDto == null) {
            stockDto = new StockDto();
            stockDto.setStockName("KOSDAQ");
            stockDto.setIndexValue("850.20");
            stockDto.setIndexChange("+5.8");
            stockDto.setChangeRate("0.68");
        }
        indices.add(stockDto);

        // KOSPI200 지수
        stockDto = stockService.getKospi200Index();
        if (stockDto == null) {
            stockDto = new StockDto();
            stockDto.setStockName("KOSPI200");
            stockDto.setIndexValue("370.45");
            stockDto.setIndexChange("+2.1");
            stockDto.setChangeRate("0.57");
        }
        indices.add(stockDto);

        return ResponseEntity.ok(indices);
    }

    @GetMapping("/top-gainers")
    public ResponseEntity<StockDto> getGainers() {
        StockDto stockDto = stockService.getGainers();
        return ResponseEntity.ok(stockDto);
    }

    @GetMapping("/top-losers")
    public ResponseEntity<StockDto> getLosers() {
        StockDto stockDto = stockService.getLosers();
        return ResponseEntity.ok(stockDto);
    }

    @GetMapping("/top-market-cap")
    public ResponseEntity<StockDto> getTopMarketCap() {
        StockDto stockDto = stockService.getTopMarketCap();
        if (stockDto == null) {
            // API 에러시 더미 데이터 반환
            stockDto = new StockDto();
            java.util.List<StockDto> marketCapList = new java.util.ArrayList<>();

            // 더미 데이터 추가
            StockDto stockDto1 = new StockDto("005930", "삼성전자", "84900", "+1400", "+1.68");
            stockDto1.setVolume("5919637922"); // 발행주식수 (상장주식수)
            marketCapList.add(stockDto1);

            StockDto stockDto2 = new StockDto("000660", "SK하이닉스", "142000", "+2000", "+1.43");
            stockDto2.setVolume("728002365"); // 발행주식수 (상장주식수)
            marketCapList.add(stockDto2);

            StockDto stockDto3 = new StockDto("373220", "LG에너지솔루션", "485000", "+15000", "+3.19");
            stockDto3.setVolume("114140000"); // 발행주식수 (상장주식수)
            marketCapList.add(stockDto3);

            stockDto.setTopMarketCap(marketCapList);
        }
        return ResponseEntity.ok(stockDto);
    }

    @GetMapping("/top-volume")
    public ResponseEntity<StockDto> getTopVolume() {
        StockDto stockDto = stockService.getTopVolume();
        return ResponseEntity.ok(stockDto);
    }

    /**
     * 종목/지수 차트 데이터 조회
     * @param stockCode 종목코드 (예: 005930) 또는 지수코드 (0001: KOSPI, 1001: KOSDAQ, 2001: KOSPI200)
     * @param timeframe 기간구분 (D: 일봉, W: 주봉, M: 월봉) - 기본값 D
     * @return ChartDataDto (종목명, 캔들 데이터 리스트)
     */
    @GetMapping("/chart/{stockCode}")
    public ResponseEntity<ChartDataDto> getStockChart(
            @PathVariable String stockCode,
            @RequestParam(defaultValue = "D") String timeframe) {
        ChartDataDto chartData;

        // 지수 코드인지 확인 (0001: KOSPI, 1001: KOSDAQ, 2001: KOSPI200)
        if (stockCode.equals("0001") || stockCode.equals("1001") || stockCode.equals("2001")) {
            chartData = stockService.getIndexChartData(stockCode, timeframe);
        } else {
            chartData = stockService.getStockChartData(stockCode, timeframe);
        }

        return ResponseEntity.ok(chartData);
    }

}