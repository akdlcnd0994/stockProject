package com.example.stockproject.service;

import com.example.stockproject.dto.CandleDto;
import com.example.stockproject.dto.ChartDataDto;
import com.example.stockproject.dto.StockDto;
import com.example.stockproject.mapper.StockMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class StockService {

    private final String APP_KEY = "PSc3l5swHONsfGybiK9JLVwYVNBwu7KiGBN6";
    private final String APP_SECRET = "TfaWrzgaxdXYzT5madBSzzETpxTynYdvivH291UtdVUVnWEDpJvVpbJmoegg2Bmec19gYyTMqk0UfP76NvdQcaFiUkOTeDum/wdznmW3YJ9MVU5ERVIN5MjagnesQyVThT4Ww5HhUUPfQ097nbQS/BodDuu1fBzX5EHZ5xDx28YQaqVvv6k=";
    private final String BASE_URL = "https://openapi.koreainvestment.com:9443"; //한투 api

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String accessToken;

    private String getAccessToken() { //토큰체크
        if (accessToken != null) {
            return accessToken;
        }

        String url = BASE_URL + "/oauth2/tokenP";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", APP_KEY);
        body.put("appsecret", APP_SECRET);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                accessToken = jsonNode.get("access_token").asText();
                return accessToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param trId 거래ID (TR_ID)
     *             - FHPUP02100000: 업종 현재가 조회 (KOSPI 전용)
     *             - FHPST01030000: 국내주식 업종기간별시세(일/주/월/년) 조회
     */
    private StockDto getIndexData(String indexName, String indexCode, String trId) {
        String token = getAccessToken();
        if (token == null) {
            System.out.println("getIndexData(" + indexName + "): Access token is null");
            return null;
        }

        String url = BASE_URL + "/uapi/domestic-stock/v1/quotations/inquire-index-price";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + token);
        headers.set("appkey", APP_KEY);
        headers.set("appsecret", APP_SECRET);
        headers.set("tr_id", trId);
        headers.set("custtype", "P");

        String queryParams = "?FID_COND_MRKT_DIV_CODE=U&FID_INPUT_ISCD=" + indexCode;
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            System.out.println("getIndexData(" + indexName + "): Making API call with code=" + indexCode + ", trId=" + trId);
            ResponseEntity<String> response = restTemplate.exchange(url + queryParams, HttpMethod.GET, request, String.class);
            System.out.println("getIndexData(" + indexName + "): Response status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String rtCd = jsonNode.get("rt_cd").asText();
                System.out.println("getIndexData(" + indexName + "): rt_cd = " + rtCd);
                System.out.println("getIndexData(" + indexName + "): Response body: " + response.getBody());

                if ("0".equals(rtCd) || "".equals(rtCd)) {
                    JsonNode output = jsonNode.get("output");
                    if (output != null) {
                        StockDto stockDto = new StockDto();
                        stockDto.setStockName(indexName);
                        // bstp_nmix_prpr: 지수 현재가
                        stockDto.setIndexValue(output.get("bstp_nmix_prpr").asText());

                        // bstp_nmix_prdy_vrss: 전일 대비 등락
                        String indexChange = output.get("bstp_nmix_prdy_vrss").asText();
                        // +/- 기호가 없으면 추가 (양수인 경우)
                        if (!indexChange.startsWith("+") && !indexChange.startsWith("-")) {
                            try {
                                double changeValue = Double.parseDouble(indexChange);
                                if (changeValue > 0) {
                                    indexChange = "+" + indexChange;
                                }
                            } catch (NumberFormatException e) {
                                // 파싱 실패시 그대로 사용
                            }
                        }
                        stockDto.setIndexChange(indexChange);

                        // bstp_nmix_prdy_ctrt: 전일 대비 등락률
                        stockDto.setChangeRate(output.get("bstp_nmix_prdy_ctrt").asText());
                        System.out.println("getIndexData(" + indexName + "): Success - value=" + stockDto.getIndexValue());
                        return stockDto;
                    } else {
                        System.out.println("getIndexData(" + indexName + "): output is null");
                    }
                } else {
                    System.out.println("getIndexData(" + indexName + "): API error - msg1: " + jsonNode.get("msg1").asText());
                }
            }
        } catch (Exception e) {
            System.out.println("getIndexData(" + indexName + "): Exception occurred");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * KOSPI 지수 조회
     * 지수코드: 0001
     * TR_ID: FHPUP02100000 (업종 현재가)
     */
    public StockDto getKospiIndex() {
        return getIndexData("KOSPI", "0001", "FHPUP02100000");
    }

    /**
     * KOSDAQ 지수 조회
     * 지수코드: 1001
     * TR_ID: FHPUP02100000 (업종 현재가)
     */
    public StockDto getKosdaqIndex() {
        return getIndexData("KOSDAQ", "1001", "FHPUP02100000");
    }

    /**
     * KOSPI200 지수 조회
     * 지수코드: 2001
     * TR_ID: FHPUP02100000 (업종 현재가)
     */
    public StockDto getKospi200Index() {
        return getIndexData("KOSPI200", "2001", "FHPUP02100000");
    }



    public StockDto getGainers() {
        String token = getAccessToken();
        if (token == null) {
            System.out.println("getGainers: Access token is null");
            return null;
        }

        String url = BASE_URL + "/uapi/domestic-stock/v1/quotations/volume-rank";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + token);
        headers.set("appkey", APP_KEY);
        headers.set("appsecret", APP_SECRET);
        headers.set("tr_id", "FHPST01710000");
        headers.set("custtype", "P");

        // 전체 시장 (코스피+코스닥) 데이터를 가져와서 상승률 기준으로 정렬
        // FID_COND_MRKT_DIV_CODE=J: 전체시장, FID_INPUT_ISCD=: 전체종목, FID_TRGT_CLS_CODE=000000000: 모든 종목
        String queryParams = "?FID_COND_MRKT_DIV_CODE=J&FID_COND_SCR_DIV_CODE=20178&FID_INPUT_ISCD=&FID_DIV_CLS_CODE=0&FID_BLNG_CLS_CODE=0&FID_TRGT_CLS_CODE=000000000&FID_TRGT_EXLS_CLS_CODE=000000&FID_INPUT_PRICE_1=&FID_INPUT_PRICE_2=&FID_VOL_CNT=200&FID_INPUT_DATE_1=";

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            System.out.println("getGainers: Making API call to " + url + queryParams);
            ResponseEntity<String> response = restTemplate.exchange(url + queryParams, HttpMethod.GET, request, String.class);
            System.out.println("getGainers: Response status: " + response.getStatusCode());
            System.out.println("getGainers: Response body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                System.out.println("getGainers: rt_cd = " + jsonNode.get("rt_cd").asText());

                String rtCd = jsonNode.get("rt_cd").asText();
                if ("0".equals(rtCd) || "".equals(rtCd)) {
                    JsonNode outputArray = jsonNode.get("output");

                    if (outputArray == null || outputArray.isNull()) {
                        System.out.println("getGainers: output field is null or missing");
                        return null;
                    }

                    System.out.println("getGainers: output array size: " + outputArray.size());

                    List<StockDto> allStocks = new ArrayList<>();

                    // 모든 종목 데이터 수집
                    for (int i = 0; i < outputArray.size(); i++) {
                        JsonNode item = outputArray.get(i);
                        String changeRate = item.get("prdy_ctrt").asText();

                        try {
                            double rate = Double.parseDouble(changeRate);
                            if (rate > 0) {  // 상승한 종목만
                                StockDto stockDto = new StockDto();
                                stockDto.setStockCode(item.get("mksc_shrn_iscd").asText());
                                stockDto.setStockName(item.get("hts_kor_isnm").asText());
                                stockDto.setCurrentPrice(item.get("stck_prpr").asText());
                                stockDto.setPreviousDayDiff(item.get("prdy_vrss").asText());
                                stockDto.setChangeRate(changeRate);
                                stockDto.setVolume(item.get("acml_vol").asText());
                                allStocks.add(stockDto);
                            }
                        } catch (NumberFormatException e) {
                            // 파싱 에러 시 무시
                        }
                    }

                    System.out.println("getGainers: 상승 종목 수: " + allStocks.size());

                    // 상승률 기준으로 내림차순 정렬 (순수 상승률 순위)
                    allStocks.sort((a, b) -> {
                        try {
                            double aRate = Double.parseDouble(a.getChangeRate());
                            double bRate = Double.parseDouble(b.getChangeRate());
                            return Double.compare(bRate, aRate);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    });

                    // 상위 3개만 선택
                    List<StockDto> gainers = allStocks.subList(0, Math.min(3, allStocks.size()));

                    // 로그로 상위 종목 확인
                    for (int i = 0; i < gainers.size(); i++) {
                        StockDto stockDto = gainers.get(i);
                        System.out.println("getGainers: " + (i+1) + "위 - " + stockDto.getStockName() + " " + stockDto.getChangeRate() + "%");
                    }

                    StockDto stockDto = new StockDto();
                    stockDto.setTopGainers(gainers);
                    return stockDto;
                } else {
                    System.out.println("getGainers: API error - rt_cd: " + rtCd);
                    System.out.println("getGainers: Error message: " + jsonNode.get("msg1").asText());
                }
            }
        } catch (Exception e) {
            System.out.println("getGainers: Exception occurred");
            e.printStackTrace();
        }

        return null;
    }

    public StockDto getLosers() {
        String token = getAccessToken();
        if (token == null) {
            System.out.println("getLosers: Access token is null");
            return null;
        }

        String url = BASE_URL + "/uapi/domestic-stock/v1/quotations/volume-rank";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + token);
        headers.set("appkey", APP_KEY);
        headers.set("appsecret", APP_SECRET);
        headers.set("tr_id", "FHPST01710000");
        headers.set("custtype", "P");

        // 상승률과 동일한 기준으로 하락률 데이터 조회 (20178 API에서 음수 필터링)
        String queryParams = "?FID_COND_MRKT_DIV_CODE=J&FID_COND_SCR_DIV_CODE=20178&FID_INPUT_ISCD=&FID_DIV_CLS_CODE=0&FID_BLNG_CLS_CODE=0&FID_TRGT_CLS_CODE=000000000&FID_TRGT_EXLS_CLS_CODE=000000&FID_INPUT_PRICE_1=&FID_INPUT_PRICE_2=&FID_VOL_CNT=200&FID_INPUT_DATE_1=";

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            System.out.println("getLosers: Making API call to " + url + queryParams);
            ResponseEntity<String> response = restTemplate.exchange(url + queryParams, HttpMethod.GET, request, String.class);
            System.out.println("getLosers: Response status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String rtCd = jsonNode.get("rt_cd").asText();
                if ("0".equals(rtCd) || "".equals(rtCd)) {
                    JsonNode outputArray = jsonNode.get("output");

                    if (outputArray == null || outputArray.isNull()) {
                        System.out.println("getLosers: output field is null or missing");
                        return null;
                    }

                    System.out.println("getLosers: output array size: " + outputArray.size());

                    List<StockDto> allStocks = new ArrayList<>();

                    for (int i = 0; i < outputArray.size(); i++) {
                        JsonNode item = outputArray.get(i);
                        String changeRate = item.get("prdy_ctrt").asText();

                        try {
                            double rate = Double.parseDouble(changeRate);
                            if (rate < 0) {
                                StockDto stockDto = new StockDto();
                                stockDto.setStockCode(item.get("mksc_shrn_iscd").asText());
                                stockDto.setStockName(item.get("hts_kor_isnm").asText());
                                stockDto.setCurrentPrice(item.get("stck_prpr").asText());
                                stockDto.setPreviousDayDiff(item.get("prdy_vrss").asText());
                                stockDto.setChangeRate(changeRate);
                                stockDto.setVolume(item.get("acml_vol").asText());
                                allStocks.add(stockDto);
                            }
                        } catch (NumberFormatException e) {
                            // 파싱 에러 시 무시
                        }
                    }

                    allStocks.sort((a, b) -> {
                        try {
                            double aRate = Math.abs(Double.parseDouble(a.getChangeRate()));
                            double bRate = Math.abs(Double.parseDouble(b.getChangeRate()));
                            return Double.compare(bRate, aRate);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    });

                    List<StockDto> losers = allStocks.subList(0, Math.min(3, allStocks.size()));

                    for (int i = 0; i < losers.size(); i++) {
                        StockDto stockDto = losers.get(i);
                        System.out.println("getLosers: " + (i+1) + "위 - " + stockDto.getStockName() + " " + stockDto.getChangeRate() + "%");
                    }

                    StockDto stockDto = new StockDto();
                    stockDto.setTopLosers(losers);
                    return stockDto;
                } else {
                    System.out.println("getLosers: API error - rt_cd: " + rtCd);
                }
            }
        } catch (Exception e) {
            System.out.println("getLosers: Exception occurred");
            e.printStackTrace();
        }

        return null;
    }

    public StockDto getTopMarketCap() {
        String token = getAccessToken();
        if (token == null) {
            System.out.println("getTopMarketCap: Access token is null");
            return null;
        }

        String url = BASE_URL + "/uapi/domestic-stock/v1/quotations/volume-rank";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + token);
        headers.set("appkey", APP_KEY);
        headers.set("appsecret", APP_SECRET);
        headers.set("tr_id", "FHPST01710000");
        headers.set("custtype", "P");

        // 전체 시장 (코스피+코스닥) 조회: FID_INPUT_ISCD= (빈값=전체)
        // 거래대금 순위 (시가총액 근사치): FID_COND_SCR_DIV_CODE=20174 (거래대금 순위)
        String queryParams = "?FID_COND_MRKT_DIV_CODE=J&FID_COND_SCR_DIV_CODE=20174&FID_INPUT_ISCD=&FID_DIV_CLS_CODE=0&FID_BLNG_CLS_CODE=0&FID_TRGT_CLS_CODE=111111111&FID_TRGT_EXLS_CLS_CODE=000000&FID_INPUT_PRICE_1=&FID_INPUT_PRICE_2=&FID_VOL_CNT=&FID_INPUT_DATE_1=";

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            System.out.println("getTopMarketCap: Making API call to " + url + queryParams);
            ResponseEntity<String> response = restTemplate.exchange(url + queryParams, HttpMethod.GET, request, String.class);
            System.out.println("getTopMarketCap: Response status: " + response.getStatusCode());
            System.out.println("getTopMarketCap: Response body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                System.out.println("getTopMarketCap: rt_cd = " + jsonNode.get("rt_cd").asText());

                String rtCd = jsonNode.get("rt_cd").asText();
                if ("0".equals(rtCd) || "".equals(rtCd)) {
                    JsonNode outputArray = jsonNode.get("output");

                    if (outputArray == null || outputArray.isNull()) {
                        System.out.println("getTopMarketCap: output field is null or missing");
                        return null;
                    }

                    System.out.println("getTopMarketCap: output array size: " + outputArray.size());

                    List<StockDto> marketCapList = new ArrayList<>();
                    for (int i = 0; i < outputArray.size(); i++) {
                        JsonNode item = outputArray.get(i);

                        // 개별 종목의 상세 정보를 조회하여 실제 시가총액 계산
                        String stockCode = item.get("mksc_shrn_iscd").asText();
                        StockDto detailInfo = getStockDetailForMarketCap(stockCode);

                        if (detailInfo != null) {
                            detailInfo.setStockCode(stockCode);
                            detailInfo.setStockName(item.get("hts_kor_isnm").asText());
                            detailInfo.setCurrentPrice(item.get("stck_prpr").asText());
                            detailInfo.setPreviousDayDiff(item.get("prdy_vrss").asText());
                            detailInfo.setChangeRate(item.get("prdy_ctrt").asText());
                            detailInfo.setVolume(item.get("acml_vol").asText());
                            detailInfo.setTradingValue(item.get("acml_tr_pbmn").asText());
                            marketCapList.add(detailInfo);
                        }
                    }

                    // 실제 시가총액이 있으면 그것으로, 없으면 거래대금 기준으로 정렬
                    marketCapList.sort((a, b) -> {
                        try {
                            // 실제 시가총액이 있는 경우
                            if (a.getMarketCap() != null && b.getMarketCap() != null) {
                                long aMarketCap = Long.parseLong(a.getMarketCap().replaceAll(",", ""));
                                long bMarketCap = Long.parseLong(b.getMarketCap().replaceAll(",", ""));
                                return Long.compare(bMarketCap, aMarketCap);
                            }
                            // 거래대금 기준 정렬 (시가총액 근사치)
                            long aValue = Long.parseLong(a.getTradingValue().replaceAll(",", ""));
                            long bValue = Long.parseLong(b.getTradingValue().replaceAll(",", ""));
                            return Long.compare(bValue, aValue);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    });

                    // 상위 5개만 선택
                    marketCapList = marketCapList.subList(0, Math.min(5, marketCapList.size()));

                    StockDto stockDto = new StockDto();
                    stockDto.setTopMarketCap(marketCapList);
                    return stockDto;
                } else {
                    System.out.println("getTopMarketCap: API error - rt_cd: " + rtCd);
                    System.out.println("getTopMarketCap: Error message: " + jsonNode.get("msg1").asText());
                }
            }
        } catch (Exception e) {
            System.out.println("getTopMarketCap: Exception occurred");
            e.printStackTrace();
        }

        return null;
    }

    private StockDto getStockDetailForMarketCap(String stockCode) {
        String token = getAccessToken();
        if (token == null) {
            return null;
        }

        String url = BASE_URL + "/uapi/domestic-stock/v1/quotations/inquire-price";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + token);
        headers.set("appkey", APP_KEY);
        headers.set("appsecret", APP_SECRET);
        headers.set("tr_id", "FHKST01010100");
        headers.set("custtype", "P");

        String queryParams = "?FID_COND_MRKT_DIV_CODE=J&FID_INPUT_ISCD=" + stockCode;

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url + queryParams, HttpMethod.GET, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                if ("0".equals(jsonNode.get("rt_cd").asText())) {
                    JsonNode output = jsonNode.get("output");

                    StockDto stockDto = new StockDto();

                    // 상장주식수와 현재가를 통한 시가총액 계산 (API쪽에서 시가총액을 제공하지 않아 계산이 필요)
                    String lstnStcn = output.get("lstn_stcn").asText(); // 상장주식수
                    String currentPrice = output.get("stck_prpr").asText(); // 현재가

                    if (lstnStcn != null && !lstnStcn.isEmpty() && currentPrice != null && !currentPrice.isEmpty()) {
                        try {
                            long listedShares = Long.parseLong(lstnStcn);
                            long price = Long.parseLong(currentPrice);
                            long marketCap = listedShares * price; // 실제 시가총액 = 상장주식수 × 현재가
                            stockDto.setMarketCap(String.valueOf(marketCap));
                        } catch (NumberFormatException e) {
                            // 계산 실패시 null로 유지
                        }
                    }

                    return stockDto;
                }
            }
        } catch (Exception e) {
            // 개별 조회 실패시 null 반환
        }

        return null;
    }

    public StockDto getTopVolume() {
        String token = getAccessToken();
        if (token == null) {
            System.out.println("getTopVolume: Access token is null");
            return null;
        }

        String url = BASE_URL + "/uapi/domestic-stock/v1/quotations/volume-rank";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + token);
        headers.set("appkey", APP_KEY);
        headers.set("appsecret", APP_SECRET);
        headers.set("tr_id", "FHPST01710000");
        headers.set("custtype", "P");

        // 전체 시장 (코스피+코스닥) 조회: FID_INPUT_ISCD= (빈값=전체)
        // 거래대금 순위: FID_COND_SCR_DIV_CODE=20174 (거래대금 순위)
        String queryParams = "?FID_COND_MRKT_DIV_CODE=J&FID_COND_SCR_DIV_CODE=20174&FID_INPUT_ISCD=&FID_DIV_CLS_CODE=0&FID_BLNG_CLS_CODE=0&FID_TRGT_CLS_CODE=111111111&FID_TRGT_EXLS_CLS_CODE=000000&FID_INPUT_PRICE_1=&FID_INPUT_PRICE_2=&FID_VOL_CNT=&FID_INPUT_DATE_1=";

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            System.out.println("getTopVolume: Making API call to " + url + queryParams);
            ResponseEntity<String> response = restTemplate.exchange(url + queryParams, HttpMethod.GET, request, String.class);
            System.out.println("getTopVolume: Response status: " + response.getStatusCode());
            System.out.println("getTopVolume: Response body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                System.out.println("getTopVolume: rt_cd = " + jsonNode.get("rt_cd").asText());

                String rtCd = jsonNode.get("rt_cd").asText();
                if ("0".equals(rtCd) || "".equals(rtCd)) {
                    JsonNode outputArray = jsonNode.get("output");

                    if (outputArray == null || outputArray.isNull()) {
                        System.out.println("getTopVolume: output field is null or missing");
                        return null;
                    }

                    System.out.println("getTopVolume: output array size: " + outputArray.size());

                    List<StockDto> volumeList = new ArrayList<>();
                    for (int i = 0; i < outputArray.size(); i++) {
                        JsonNode item = outputArray.get(i);
                        StockDto stockDto = new StockDto();
                        stockDto.setStockCode(item.get("mksc_shrn_iscd").asText());
                        stockDto.setStockName(item.get("hts_kor_isnm").asText());
                        stockDto.setCurrentPrice(item.get("stck_prpr").asText());
                        stockDto.setPreviousDayDiff(item.get("prdy_vrss").asText());
                        stockDto.setChangeRate(item.get("prdy_ctrt").asText());
                        stockDto.setVolume(item.get("acml_vol").asText());
                        stockDto.setTradingValue(item.get("acml_tr_pbmn").asText());
                        volumeList.add(stockDto);
                    }

                    // 거래대금 기준으로 내림차순 정렬
                    volumeList.sort((a, b) -> {
                        try {
                            long aTradingValue = Long.parseLong(a.getTradingValue().replaceAll(",", ""));
                            long bTradingValue = Long.parseLong(b.getTradingValue().replaceAll(",", ""));
                            return Long.compare(bTradingValue, aTradingValue);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    });

                    // 상위 5개만 선택
                    volumeList = volumeList.subList(0, Math.min(5, volumeList.size()));

                    StockDto stockDto = new StockDto();
                    stockDto.setTopVolume(volumeList);
                    return stockDto;
                } else {
                    System.out.println("getTopVolume: API error - rt_cd: " + rtCd);
                    System.out.println("getTopVolume: Error message: " + jsonNode.get("msg1").asText());
                }
            }
        } catch (Exception e) {
            System.out.println("getTopVolume: Exception occurred");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 종목 차트 데이터 조회 (일봉/주봉/월봉)
     *
     * @param stockCode 종목코드 (예: "005930" = 삼성전자)
     * @param timeframe 기간 구분 (D: 일봉, W: 주봉, M: 월봉, Y: 년봉)
     * @return ChartDataDto (종목명, 캔들 데이터 리스트 포함)
     *
     * TR_ID: FHKST03010100 (국내주식 기간별 시세)
     * API: /uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice
     */
    public ChartDataDto getStockChartData(String stockCode, String timeframe) {
        String token = getAccessToken();
        if (token == null) {
            System.out.println("getStockChartData: Access token is null");
            return null;
        }

        String url = BASE_URL + "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + token);
        headers.set("appkey", APP_KEY);
        headers.set("appsecret", APP_SECRET);
        headers.set("tr_id", "FHKST03010100"); // 국내주식 기간별 시세
        headers.set("custtype", "P");

        // 종료일: 오늘
        LocalDate endDate = LocalDate.now();
        // 시작일: timeframe에 따라 다르게 설정
        LocalDate startDate;
        switch (timeframe.toUpperCase()) {
            case "W": // 주봉: 최근 6개월
                startDate = endDate.minusMonths(6);
                break;
            case "M": // 월봉: 최근 2년
                startDate = endDate.minusYears(2);
                break;
            case "Y": // 년봉: 최근 10년
                startDate = endDate.minusYears(10);
                break;
            default: // D (일봉): 최근 3개월
                startDate = endDate.minusMonths(3);
                break;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        // API 파라미터
        String queryParams = String.format(
            "?FID_COND_MRKT_DIV_CODE=J&FID_INPUT_ISCD=%s&FID_INPUT_DATE_1=%s&FID_INPUT_DATE_2=%s&FID_PERIOD_DIV_CODE=%s&FID_ORG_ADJ_PRC=0",
            stockCode, startDateStr, endDateStr, timeframe
        );

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            System.out.println("getStockChartData: Making API call for stock=" + stockCode + ", timeframe=" + timeframe);
            ResponseEntity<String> response = restTemplate.exchange(url + queryParams, HttpMethod.GET, request, String.class);
            System.out.println("getStockChartData: Response status: " + response.getStatusCode());
            System.out.println("getStockChartData: Full response body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String rtCd = jsonNode.get("rt_cd").asText();
                System.out.println("getStockChartData: rt_cd = " + rtCd);

                if ("0".equals(rtCd) || "".equals(rtCd)) {
                    JsonNode output1 = jsonNode.get("output1"); // 종목 정보
                    JsonNode output2 = jsonNode.get("output2"); // 차트 데이터 배열

                    if (output1 == null || output2 == null) {
                        System.out.println("getStockChartData: output1 or output2 is null");
                        return null;
                    }

                    // 종목명 가져오기
                    String stockName = output1.get("hts_kor_isnm").asText();
                    System.out.println("getStockChartData: Stock name = " + stockName);
                    System.out.println("getStockChartData: Candle data count = " + output2.size());

                    // 첫 번째 캔들 데이터 구조 확인
                    if (output2.size() > 0) {
                        System.out.println("getStockChartData: First candle structure: " + output2.get(0).toString());
                    }

                    // 캔들 데이터 리스트 생성
                    List<CandleDto> candles = new ArrayList<>();
                    for (JsonNode candle : output2) {
                        CandleDto candleDto = new CandleDto();

                        // Null safe 처리
                        candleDto.setDate(candle.has("stck_bsop_date") ? candle.get("stck_bsop_date").asText() : "");
                        candleDto.setOpen(candle.has("stck_oprc") ? candle.get("stck_oprc").asText() : "0");
                        candleDto.setHigh(candle.has("stck_hgpr") ? candle.get("stck_hgpr").asText() : "0");
                        candleDto.setLow(candle.has("stck_lwpr") ? candle.get("stck_lwpr").asText() : "0");
                        candleDto.setClose(candle.has("stck_clpr") ? candle.get("stck_clpr").asText() : "0");
                        candleDto.setVolume(candle.has("acml_vol") ? candle.get("acml_vol").asText() : "0");
                        candleDto.setChangeRate(candle.has("prdy_ctrt") ? candle.get("prdy_ctrt").asText() : "0");

                        candles.add(candleDto);
                    }

                    // ChartDataDto 생성 및 반환
                    ChartDataDto chartData = new ChartDataDto(stockCode, stockName, timeframe, candles);
                    return chartData;
                } else {
                    System.out.println("getStockChartData: API error - msg1: " + jsonNode.get("msg1").asText());
                }
            }
        } catch (Exception e) {
            System.out.println("getStockChartData: Exception occurred");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 지수 차트 데이터 조회
     *
     * @param indexCode 지수 코드 (0001: KOSPI, 1001: KOSDAQ, 2001: KOSPI200)
     * @param timeframe 기간 구분 (D: 일봉, W: 주봉, M: 월봉)
     * @return ChartDataDto (지수명, 캔들 데이터 리스트 포함)
     *
     * TR_ID: FHKUP03500100 (업종기간별시세)
     * API: /uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice
     */
    public ChartDataDto getIndexChartData(String indexCode, String timeframe) {
        String token = getAccessToken();
        if (token == null) {
            System.out.println("getIndexChartData: Access token is null");
            return null;
        }

        String url = BASE_URL + "/uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + token);
        headers.set("appkey", APP_KEY);
        headers.set("appsecret", APP_SECRET);
        headers.set("tr_id", "FHKUP03500100"); // 업종기간별시세
        headers.set("custtype", "P");

        // 종료일: 오늘
        LocalDate endDate = LocalDate.now();
        // 시작일: timeframe에 따라 다르게 설정
        LocalDate startDate;
        switch (timeframe.toUpperCase()) {
            case "W": // 주봉: 최근 6개월
                startDate = endDate.minusMonths(6);
                break;
            case "M": // 월봉: 최근 2년
                startDate = endDate.minusYears(2);
                break;
            default: // D (일봉): 최근 3개월
                startDate = endDate.minusMonths(3);
                break;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        // 지수명 매핑
        String indexName;
        switch (indexCode) {
            case "0001":
                indexName = "KOSPI";
                break;
            case "1001":
                indexName = "KOSDAQ";
                break;
            case "2001":
                indexName = "KOSPI200";
                break;
            default:
                indexName = "지수";
                break;
        }

        // API 파라미터
        String queryParams = String.format(
            "?FID_COND_MRKT_DIV_CODE=U&FID_INPUT_ISCD=%s&FID_INPUT_DATE_1=%s&FID_INPUT_DATE_2=%s&FID_PERIOD_DIV_CODE=%s",
            indexCode, startDateStr, endDateStr, timeframe
        );

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            System.out.println("getIndexChartData: Making API call for index=" + indexCode + ", timeframe=" + timeframe);
            ResponseEntity<String> response = restTemplate.exchange(url + queryParams, HttpMethod.GET, request, String.class);
            System.out.println("getIndexChartData: Response status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String rtCd = jsonNode.get("rt_cd").asText();
                System.out.println("getIndexChartData: rt_cd = " + rtCd);

                if ("0".equals(rtCd) || "".equals(rtCd)) {
                    JsonNode output2 = jsonNode.get("output2"); // 차트 데이터 배열

                    if (output2 == null) {
                        System.out.println("getIndexChartData: output2 is null");
                        return null;
                    }

                    System.out.println("getIndexChartData: Index name = " + indexName);
                    System.out.println("getIndexChartData: Candle data count = " + output2.size());

                    // 캔들 데이터 리스트 생성
                    List<CandleDto> candles = new ArrayList<>();
                    for (JsonNode candle : output2) {
                        CandleDto candleDto = new CandleDto();

                        // 지수 차트 API 응답 필드명
                        candleDto.setDate(candle.has("stck_bsop_date") ? candle.get("stck_bsop_date").asText() : "");
                        candleDto.setOpen(candle.has("bstp_nmix_oprc") ? candle.get("bstp_nmix_oprc").asText() : "0");
                        candleDto.setHigh(candle.has("bstp_nmix_hgpr") ? candle.get("bstp_nmix_hgpr").asText() : "0");
                        candleDto.setLow(candle.has("bstp_nmix_lwpr") ? candle.get("bstp_nmix_lwpr").asText() : "0");
                        candleDto.setClose(candle.has("bstp_nmix_prpr") ? candle.get("bstp_nmix_prpr").asText() : "0");
                        candleDto.setVolume(candle.has("acml_vol") ? candle.get("acml_vol").asText() : "0");
                        candleDto.setChangeRate(candle.has("prdy_ctrt") ? candle.get("prdy_ctrt").asText() : "0");

                        candles.add(candleDto);
                    }

                    // ChartDataDto 생성 및 반환
                    ChartDataDto chartData = new ChartDataDto(indexCode, indexName, timeframe, candles);
                    return chartData;
                } else {
                    System.out.println("getIndexChartData: API error - msg1: " + jsonNode.get("msg1").asText());
                }
            }
        } catch (Exception e) {
            System.out.println("getIndexChartData: Exception occurred");
            e.printStackTrace();
        }

        return null;
    }
}