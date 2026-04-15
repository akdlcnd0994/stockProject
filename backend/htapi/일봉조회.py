#!/usr/bin/env python3
"""
일봉/분봉 데이터 조회 API
종목의 과거 가격 데이터를 일/주/월 단위로 조회합니다.
"""
import requests
import json
import os
from datetime import datetime, timedelta

# API 정보
APP_KEY = "PSc3l5swHONsfGybiK9JLVwYVNBwu7KiGBN6"
APP_SECRET = "TfaWrzgaxdXYzT5madBSzzETpxTynYdvivH291UtdVUVnWEDpJvVpbJmoegg2Bmec19gYyTMqk0UfP76NvdQcaFiUkOTeDum/wdznmW3YJ9MVU5ERVIN5MjagnesQyVThT4Ww5HhUUPfQ097nbQS/BodDuu1fBzX5EHZ5xDx28YQaqVvv6k="

def get_access_token():
    """토큰 파일에서 읽기 또는 새로 발급"""
    if os.path.exists("token.txt"):
        with open("token.txt", "r") as f:
            return f.read().strip()
    else:
        # 토큰 발급
        url = "https://openapi.koreainvestment.com:9443/oauth2/tokenP"
        headers = {"Content-Type": "application/json"}
        body = {
            "grant_type": "client_credentials",
            "appkey": APP_KEY,
            "appsecret": APP_SECRET
        }
        response = requests.post(url, headers=headers, data=json.dumps(body))
        if response.status_code == 200:
            token = response.json()["access_token"]
            with open("token.txt", "w") as f:
                f.write(token)
            return token
        return None

def get_daily_price(stock_code="005930", period="D"):  # D:일봉, W:주봉, M:월봉
    """일봉/분봉 데이터 조회"""
    token = get_access_token()
    if not token:
        print("토큰 발급 실패")
        return None
    
    url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-daily-price"
    
    headers = {
        "Content-Type": "application/json; charset=UTF-8",
        "authorization": f"Bearer {token}",
        "appkey": APP_KEY,
        "appsecret": APP_SECRET,
        "tr_id": "FHKST01010400",
        "custtype": "P"
    }
    
    # 조회 기간 설정 (최근 30일)
    end_date = datetime.now().strftime("%Y%m%d")
    start_date = (datetime.now() - timedelta(days=30)).strftime("%Y%m%d")
    
    params = {
        "FID_COND_MRKT_DIV_CODE": "J",  # 주식
        "FID_INPUT_ISCD": stock_code,
        "FID_PERIOD_DIV_CODE": period,  # D:일봉, W:주봉, M:월봉
        "FID_ORG_ADJ_PRC": "1"  # 수정주가 적용
    }
    
    response = requests.get(url, headers=headers, params=params)
    
    if response.status_code == 200:
        data = response.json()
        if data["rt_cd"] == "0":
            output = data.get("output", [])
            
            period_name = {"D": "일봉", "W": "주봉", "M": "월봉"}.get(period, "일봉")
            print(f"\n=== {stock_code} {period_name} 데이터 (최근 30개) ===")
            print(f"{'일자':^12} {'시가':>8} {'고가':>8} {'저가':>8} {'종가':>8} {'거래량':>12} {'대비':>8}")
            print("-" * 80)
            
            # 최근 30개 데이터만 출력
            for item in output[:30]:
                date = item.get('stck_bsop_date', 'N/A')
                if date != 'N/A' and len(date) == 8:
                    date = f"{date[:4]}-{date[4:6]}-{date[6:]}"
                
                open_price = item.get('stck_oprc', 'N/A')
                high_price = item.get('stck_hgpr', 'N/A')
                low_price = item.get('stck_lwpr', 'N/A')
                close_price = item.get('stck_clpr', 'N/A')
                volume = item.get('acml_vol', 'N/A')
                change = item.get('prdy_vrss', 'N/A')
                
                print(f"{date:^12} {open_price:>8} {high_price:>8} {low_price:>8} {close_price:>8} {volume:>12} {change:>8}")
            
            return output
        else:
            print(f"API 오류: {data.get('msg1', 'Unknown error')}")
    else:
        print(f"요청 실패: {response.status_code}")
    
    return None

if __name__ == "__main__":
    import sys
    
    # 명령줄 인자 처리
    stock_code = "005930"  # 기본값: 삼성전자
    period = "D"  # 기본값: 일봉
    
    if len(sys.argv) > 1:
        stock_code = sys.argv[1]
    if len(sys.argv) > 2:
        period = sys.argv[2].upper()
        if period not in ["D", "W", "M"]:
            print("기간 구분은 D(일봉), W(주봉), M(월봉) 중 하나여야 합니다.")
            period = "D"
    
    period_name = {"D": "일봉", "W": "주봉", "M": "월봉"}.get(period, "일봉")
    print(f"종목코드 {stock_code} {period_name} 데이터 조회 중...")
    get_daily_price(stock_code, period)