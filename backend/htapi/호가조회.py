#!/usr/bin/env python3
"""
호가 조회 API
종목의 10단계 매도/매수 호가 및 잔량 정보를 조회합니다.
"""
import requests
import json
import os

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

def get_asking_price(stock_code="005930"):  # 기본값: 삼성전자
    """호가 조회"""
    token = get_access_token()
    if not token:
        print("토큰 발급 실패")
        return None
    
    url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn"
    
    headers = {
        "Content-Type": "application/json; charset=UTF-8",
        "authorization": f"Bearer {token}",
        "appkey": APP_KEY,
        "appsecret": APP_SECRET,
        "tr_id": "FHKST01010200",
        "custtype": "P"
    }
    
    params = {
        "FID_COND_MRKT_DIV_CODE": "J",  # 주식
        "FID_INPUT_ISCD": stock_code
    }
    
    response = requests.get(url, headers=headers, params=params)
    
    if response.status_code == 200:
        data = response.json()
        if data["rt_cd"] == "0":
            output1 = data.get("output1", {})
            output2 = data.get("output2", {})
            
            print(f"\n=== {stock_code} 호가 정보 ===")
            print(f"{'매도호가':>12} {'매도잔량':>12} | {'매수잔량':>12} {'매수호가':>12}")
            print("-" * 60)
            
            # 10단계 호가 출력
            for i in range(1, 11):
                ask_price = output1.get(f"askp{i}", "N/A")
                ask_vol = output1.get(f"askp_rsqn{i}", "N/A")
                bid_price = output1.get(f"bidp{i}", "N/A")
                bid_vol = output1.get(f"bidp_rsqn{i}", "N/A")
                
                print(f"{ask_price:>12} {ask_vol:>12} | {bid_vol:>12} {bid_price:>12}")
            
            print("-" * 60)
            print(f"총 매도잔량: {output1.get('total_askp_rsqn', 'N/A'):>12}")
            print(f"총 매수잔량: {output1.get('total_bidp_rsqn', 'N/A'):>12}")
            
            if output2:
                print(f"\n현재가: {output2.get('stck_prpr', 'N/A')}원")
                print(f"전일대비: {output2.get('prdy_vrss', 'N/A')}원 ({output2.get('prdy_ctrt', 'N/A')}%)")
            
            return data["output1"]
        else:
            print(f"API 오류: {data.get('msg1', 'Unknown error')}")
    else:
        print(f"요청 실패: {response.status_code}")
    
    return None

if __name__ == "__main__":
    import sys
    
    # 명령줄 인자로 종목코드 받기 (선택사항)
    stock_code = sys.argv[1] if len(sys.argv) > 1 else "005930"
    
    print(f"종목코드 {stock_code} 호가 조회 중...")
    get_asking_price(stock_code)