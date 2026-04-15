package com.example.stockproject.scheduler;

import com.example.stockproject.dto.StockDto;
import com.example.stockproject.dto.TimePriceDto;
import com.example.stockproject.mapper.TimePriceMapper;
import com.example.stockproject.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class PriceScheduler {

    @Autowired
    private StockService stockService;

    @Autowired
    private TimePriceMapper timePriceMapper;

    /**
     * 매 시 정각에 실행 (평일 9시~15시 사이에만 실행하도록 제한 가능)
     * cron: "초 분 시 일 월 요일"
     * 0 0 * * * * : 매 시 정각 (0분 0초)
     */
    @Scheduled(cron = "0 * * * * *")
    public void savePricesEveryHour() {
        System.out.println("=== PriceScheduler 실행 시작: " + LocalTime.now() + " ===");

        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // 평일 장 시간(9시~15시)에만 실행하도록 필터링 (선택사항)
        int hour = currentTime.getHour();
        if (hour < 9 || hour >= 15) {
            System.out.println("장 시간이 아니므로 스케줄러 종료");
            return;
        }

        List<TimePriceDto> priceList = new ArrayList<>();

        // 1. KOSPI 지수 저장
        try {
            StockDto kospi = stockService.getKospiIndex();
            if (kospi != null && kospi.getIndexValue() != null) {
                TimePriceDto dto = new TimePriceDto(
                    "KOSPI",
                    today,
                    currentTime,
                    kospi.getIndexValue()
                );
                priceList.add(dto);
                System.out.println("KOSPI 저장: " + kospi.getIndexValue());
            }
        } catch (Exception e) {
            System.err.println("KOSPI 조회 실패: " + e.getMessage());
        }

        // 2. KOSDAQ 지수 저장
        try {
            StockDto kosdaq = stockService.getKosdaqIndex();
            if (kosdaq != null && kosdaq.getIndexValue() != null) {
                TimePriceDto dto = new TimePriceDto(
                    "KOSDAQ",
                    today,
                    currentTime,
                    kosdaq.getIndexValue()
                );
                priceList.add(dto);
                System.out.println("KOSDAQ 저장: " + kosdaq.getIndexValue());
            }
        } catch (Exception e) {
            System.err.println("KOSDAQ 조회 실패: " + e.getMessage());
        }

        // 3. KOSPI200 지수 저장
        try {
            StockDto kospi200 = stockService.getKospi200Index();
            if (kospi200 != null && kospi200.getIndexValue() != null) {
                TimePriceDto dto = new TimePriceDto(
                    "KOSPI200",
                    today,
                    currentTime,
                    kospi200.getIndexValue()
                );
                priceList.add(dto);
                System.out.println("KOSPI200 저장: " + kospi200.getIndexValue());
            }
        } catch (Exception e) {
            System.err.println("KOSPI200 조회 실패: " + e.getMessage());
        }

        // API 호출 간 딜레이 (Rate Limit 방지)
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 4. 시가총액 TOP 종목 저장
        try {
            StockDto marketCapData = stockService.getTopMarketCap();
            if (marketCapData != null && marketCapData.getTopMarketCap() != null) {
                for (StockDto stock : marketCapData.getTopMarketCap()) {
                    if (stock.getStockName() != null && stock.getCurrentPrice() != null) {
                        TimePriceDto dto = new TimePriceDto(
                            stock.getStockName(),
                            today,
                            currentTime,
                            stock.getCurrentPrice()
                        );
                        priceList.add(dto);
                        System.out.println(stock.getStockName() + " 저장: " + stock.getCurrentPrice());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("시가총액 TOP 조회 실패: " + e.getMessage());
        }

        // 5. DB에 일괄 저장
        if (!priceList.isEmpty()) {
            try {
                timePriceMapper.insertTimePriceList(priceList);
                System.out.println("총 " + priceList.size() + "개 데이터 DB 저장 완료");
            } catch (Exception e) {
                System.err.println("DB 저장 실패: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("저장할 데이터가 없습니다.");
        }

        System.out.println("=== PriceScheduler 실행 완료: " + LocalTime.now() + " ===\n");
    }

    /**
     * 테스트용 메서드: 즉시 실행 (필요시 주석 해제)
     * 애플리케이션 시작 후 10초 뒤에 한 번 실행
     */
    // @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 10000)
    // public void testRun() {
    //     savePricesEveryHour();
    // }
}
