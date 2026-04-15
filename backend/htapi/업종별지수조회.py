#!/usr/bin/env python3
"""
업종별 지수 조회 API (수정 버전)
코스피/코스닥의 업종별 지수를 조회합니다.
"""
import requests
import json
import os

# API 정보
APP_KEY = "PSc3l5swHONsfGybiK9JLVwYVNBwu7KiGBN6"
APP_SECRET = "TfaWrzgaxdXYzT5madBSzzETpxTynYdvivH291UtdVUVnWEDpJvVpbJmoegg2Bmec19gYyTMqk0UfP76NvdQcaFiUkOTeDum/wdznmW3YJ9MVU5ERVIN5MjagnesQyVThT4Ww5HhUUPfQ097nbQS/BodDuu1fBzX5EHZ5xDx28YQaqVvv6k="

# 업종 코드 (한국투자증권 업종 코드)
SECTOR_CODES = {
    "종합주가지수": "0001",
    "대형주": "0002",
    "중형주": "0003",
    "소형주": "0004",
    "음식료품": "0005",
    "섬유의복": "0006",
    "종이목재": "0007",
    "화학": "0008",
    "의약품": "0009",
    "비금속광물": "0010",
    "철강금속": "0011",
    "기계": "0012",
    "전기전자": "0013",
    "의료정밀": "0014",
    "운수장비": "0015",
    "유통업": "0016",
    "전기가스업": "0017",
    "건설업": "0018",
    "운수창고업": "0019",
    "통신업": "0020",
    "금융업": "0021",
    "은행": "0022",
    "증권": "0024",
    "보험": "0025",
    "서비스업": "0026",
    "제조업": "0027"
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

def get_sector_index(sector_name="전기전자"):
    """업종 지수 조회 - 일반 지수 조회 API 사용"""
    token = get_access_token()
    if not token:
        print("토큰 발급 실패")
        return None
    
    # 업종 코드 확인
    sector_code = SECTOR_CODES.get(sector_name, "0013")
    
    # 일반 지수 조회 API 사용
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
        "FID_INPUT_ISCD": sector_code   # 업종 코드
    }
    
    response = requests.get(url, headers=headers, params=params)
    
    print(f"업종: {sector_name} (코드: {sector_code})")
    
    if response.status_code == 200:
        data = response.json()
        
        if data.get("rt_cd") == "0":
            output = data.get("output", {})
            
            # 업종 지수 정보 출력
            print(f"\n=== {sector_name} 업종 지수 ===")
            print(f"현재 지수: {output.get('bstp_nmix_prpr', 'N/A')} 포인트")
            print(f"전일대비: {output.get('bstp_nmix_prdy_vrss', 'N/A')} 포인트")
            print(f"등락률: {output.get('bstp_nmix_prdy_ctrt', 'N/A')}%")
            print(f"시가: {output.get('bstp_nmix_oprc', 'N/A')} 포인트")
            print(f"고가: {output.get('bstp_nmix_hgpr', 'N/A')} 포인트")
            print(f"저가: {output.get('bstp_nmix_lwpr', 'N/A')} 포인트")
            print(f"누적거래량: {output.get('acml_vol', 'N/A')}주")
            
            return output
        else:
            print(f"API 오류: {data.get('msg1', 'Unknown error')}")
            print(f"오류 코드: {data.get('rt_cd')}, 메시지 코드: {data.get('msg_cd')}")
    else:
        print(f"HTTP 요청 실패: {response.status_code}")
    
    return None

def get_major_sectors():
    """주요 업종 지수 일괄 조회"""
    print("=== 코스피 주요 업종별 지수 ===")
    print("-" * 50)
    
    # 주요 업종만 선택
    major_sectors = ["전기전자", "화학", "의약품", "금융업", "제조업", "서비스업"]
    
    for sector in major_sectors:
        if sector in SECTOR_CODES:
            get_sector_index(sector)
            print("-" * 50)
        else:
            print(f"{sector} 업종 코드를 찾을 수 없습니다.")
            print("-" * 50)

if __name__ == "__main__":
    import sys
    
    if len(sys.argv) > 1:
        # 특정 업종 조회
        sector_name = sys.argv[1]
        
        if sector_name in SECTOR_CODES:
            get_sector_index(sector_name)
        else:
            print(f"지원되는 업종: {', '.join(SECTOR_CODES.keys())}")
            print("예: python3 업종지수조회_수정.py 전기전자")
    else:
        # 주요 업종 지수 조회
        get_major_sectors()