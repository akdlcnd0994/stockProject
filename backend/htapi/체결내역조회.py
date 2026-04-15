#!/usr/bin/env python3
"""
체결내역 조회 API
종목의 실시간 체결 거래 내역을 조회합니다.
"""
import requests
import json
import os
from datetime import datetime

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

def get_trade_history(stock_code="005930"):  # 기본값: 삼성전자
    """체결내역 조회"""
    token = get_access_token()
    if not token:
        print("토큰 발급 실패")
        return None
    
    url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-ccnl"
    
    headers = {
        "Content-Type": "application/json; charset=UTF-8",
        "authorization": f"Bearer {token}",
        "appkey": APP_KEY,
        "appsecret": APP_SECRET,
        "tr_id": "FHKST01010300",
        "custtype": "P"
    }
    
    # 체결 시간 설정 (당일 데이터)
    current_time = datetime.now().strftime("%H%M%S")
    
    params = {
        "FID_COND_MRKT_DIV_CODE": "J",  # 주식
        "FID_INPUT_ISCD": stock_code
    }
    
    response = requests.get(url, headers=headers, params=params)
    
    if response.status_code == 200:
        data = response.json()
        if data["rt_cd"] == "0":
            output = data.get("output", [])
            
            print(f"\n=== {stock_code} 체결내역 (최근 30건) ===")
            print(f"{'체결시간':^10} {'체결가':>8} {'체결량':>10} {'누적거래량':>12} {'매도/매수':^10}")
            print("-" * 65)
            
            # 최근 30건 출력
            for item in output[:30]:
                time = item.get('stck_cntg_hour', 'N/A')
                if time != 'N/A' and len(time) == 6:
                    time = f"{time[:2]}:{time[2:4]}:{time[4:]}"
                
                price = item.get('stck_prpr', 'N/A')
                volume = item.get('cntg_vol', 'N/A')
                total_volume = item.get('acml_vol', 'N/A')
                
                # 매도/매수 구분
                buy_sell = item.get('askp_rsqn_cntg_yn', '')
                if buy_sell == '1':
                    buy_sell_str = "매도"
                elif buy_sell == '2':
                    buy_sell_str = "매수"
                else:
                    buy_sell_str = "-"
                
                # 체결강도 표시
                trade_power = item.get('trad_pbct', 'N/A')
                
                print(f"{time:^10} {price:>8} {volume:>10} {total_volume:>12} {buy_sell_str:^10}")
            
            # 요약 정보
            if len(output) > 0:
                print("-" * 65)
                print(f"체결강도: {output[0].get('trad_pbct', 'N/A')}%")
                print(f"누적거래량: {output[0].get('acml_vol', 'N/A')}주")
                print(f"누적거래대금: {output[0].get('acml_tr_pbmn', 'N/A')}원")
            
            return output
        else:
            print(f"API 오류: {data.get('msg1', 'Unknown error')}")
    else:
        print(f"요청 실패: {response.status_code}")
        print(response.text)
    
    return None

if __name__ == "__main__":
    import sys
    
    # 명령줄 인자로 종목코드 받기 (선택사항)
    stock_code = sys.argv[1] if len(sys.argv) > 1 else "005930"
    
    print(f"종목코드 {stock_code} 체결내역 조회 중...")
    get_trade_history(stock_code)