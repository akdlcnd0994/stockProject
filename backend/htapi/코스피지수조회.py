#!/usr/bin/env python3
"""
코스피/코스닥 지수 조회 API
주요 지수(KOSPI, KOSDAQ, KOSPI200)의 현재 시세를 조회합니다.
"""
import requests
import json
import os

# API 정보
APP_KEY = "PSc3l5swHONsfGybiK9JLVwYVNBwu7KiGBN6"
APP_SECRET = "TfaWrzgaxdXYzT5madBSzzETpxTynYdvivH291UtdVUVnWEDpJvVpbJmoegg2Bmec19gYyTMqk0UfP76NvdQcaFiUkOTeDum/wdznmW3YJ9MVU5ERVIN5MjagnesQyVThT4Ww5HhUUPfQ097nbQS/BodDuu1fBzX5EHZ5xDx28YQaqVvv6k="

# 지수 코드 매핑
INDEX_CODES = {
    "KOSPI": "0001",     # 코스피
    "KOSDAQ": "1001",    # 코스닥
    "KOSPI200": "2001",  # 코스피200
}

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

def get_index_price(index_name="KOSPI"):
    """지수 현재가 조회"""
    token = get_access_token()
    if not token:
        print("토큰 발급 실패")
        return None
    
    # 지수 코드 확인
    index_code = INDEX_CODES.get(index_name.upper(), "0001")
    
    url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-index-price"
    
    headers = {
        "Content-Type": "application/json; charset=UTF-8",
        "authorization": f"Bearer {token}",
        "appkey": APP_KEY,
        "appsecret": APP_SECRET,
        "tr_id": "FHPUP02100000",  # 지수 조회용 tr_id
        "custtype": "P"
    }
    
    params = {
        "FID_COND_MRKT_DIV_CODE": "U",  # 지수용 구분코드
        "FID_INPUT_ISCD": index_code
    }
    
    response = requests.get(url, headers=headers, params=params)
    
    if response.status_code == 200:
        data = response.json()
        if data["rt_cd"] == "0":
            output = data.get("output", {})
            
            # 지수 정보 출력
            print(f"\n=== {index_name} 지수 정보 ===")
            print(f"현재 지수: {output.get('bstp_nmix_prpr', 'N/A')} 포인트")
            print(f"전일대비: {output.get('bstp_nmix_prdy_vrss', 'N/A')} 포인트")
            print(f"등락률: {output.get('bstp_nmix_prdy_ctrt', 'N/A')}%")
            print(f"시가: {output.get('bstp_nmix_oprc', 'N/A')} 포인트")
            print(f"고가: {output.get('bstp_nmix_hgpr', 'N/A')} 포인트")
            print(f"저가: {output.get('bstp_nmix_lwpr', 'N/A')} 포인트")
            print(f"누적거래량: {output.get('acml_vol', 'N/A')}주")
            print(f"누적거래대금: {output.get('acml_tr_pbmn', 'N/A')}백만원")
            
            return output
        else:
            print(f"API 오류: {data.get('msg1', 'Unknown error')}")
    else:
        print(f"요청 실패: {response.status_code}")
        print(response.text)
    
    return None

def get_all_major_indices():
    """모든 주요 지수 조회"""
    print("=== 한국 주요 지수 현황 ===")
    print("-" * 50)
    
    for index_name in INDEX_CODES.keys():
        get_index_price(index_name)
        print("-" * 50)

if __name__ == "__main__":
    import sys
    
    if len(sys.argv) > 1:
        # 특정 지수 조회
        index_name = sys.argv[1].upper()
        if index_name in INDEX_CODES:
            print(f"{index_name} 지수 조회 중...")
            get_index_price(index_name)
        else:
            print(f"지원되는 지수: {', '.join(INDEX_CODES.keys())}")
            print("예: python3 코스피지수조회.py KOSPI")
    else:
        # 모든 주요 지수 조회
        get_all_major_indices()