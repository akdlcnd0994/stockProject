#!/usr/bin/env python3
"""
하락률 순위 조회 API
코스피/코스닥 하락률 상위 종목을 조회합니다.
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

def get_top_losers(market="J", limit=10):
    """하락률 상위 종목 조회

    Args:
        market: J(코스피), Q(코스닥), K(코넥스)
        limit: 조회할 종목 수
    """
    token = get_access_token()
    if not token:
        print("토큰 발급 실패")
        return None

    # 하락률 순위 조회 API
    url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/volume-rank"

    headers = {
        "Content-Type": "application/json; charset=UTF-8",
        "authorization": f"Bearer {token}",
        "appkey": APP_KEY,
        "appsecret": APP_SECRET,
        "tr_id": "FHPST01710000",  # 거래량 순위
        "custtype": "P"
    }

    params = {
        "FID_COND_MRKT_DIV_CODE": market,
        "FID_COND_SCR_DIV_CODE": "20170",  # 하락률 순위
        "FID_INPUT_ISCD": "0000",
        "FID_DIV_CLS_CODE": "0",
        "FID_BLNG_CLS_CODE": "0",
        "FID_TRGT_CLS_CODE": "111111111",
        "FID_TRGT_EXLS_CLS_CODE": "000000",
        "FID_INPUT_PRICE_1": "",
        "FID_INPUT_PRICE_2": "",
        "FID_VOL_CNT": "",
        "FID_INPUT_DATE_1": ""
    }

    response = requests.get(url, headers=headers, params=params)

    if response.status_code == 200:
        data = response.json()
        print(f"Response: {data}")  # 디버깅용

        if data.get("rt_cd") == "0" or data.get("rt_cd") == "":
            output = data.get("output", [])

            print(f"\n=== 하락률 상위 {min(limit, len(output))}종목 ===")

            result = []
            for i, item in enumerate(output[:limit]):
                stock_info = {
                    "rank": i + 1,
                    "stock_code": item.get("mksc_shrn_iscd", ""),
                    "stock_name": item.get("hts_kor_isnm", ""),
                    "current_price": item.get("stck_prpr", ""),
                    "change": item.get("prdy_vrss", ""),
                    "change_rate": item.get("prdy_ctrt", ""),
                    "volume": item.get("acml_vol", "")
                }

                print(f"{stock_info['rank']}. {stock_info['stock_name']} ({stock_info['stock_code']})")
                print(f"   현재가: {stock_info['current_price']}원")
                print(f"   등락: {stock_info['change']}원 ({stock_info['change_rate']}%)")
                print(f"   거래량: {stock_info['volume']}주")
                print()

                result.append(stock_info)

            return result
        else:
            print(f"API 오류: {data.get('msg1', 'Unknown error')}")
            print(f"전체 응답: {data}")
    else:
        print(f"요청 실패: {response.status_code}")
        print(f"응답: {response.text}")

    return None

if __name__ == "__main__":
    import sys

    # 명령줄 인자 처리
    market = sys.argv[1] if len(sys.argv) > 1 else "J"  # 기본값: 코스피
    limit = int(sys.argv[2]) if len(sys.argv) > 2 else 5  # 기본값: 5개

    market_name = {"J": "코스피", "Q": "코스닥", "K": "코넥스"}.get(market, "코스피")
    print(f"{market_name} 하락률 상위 {limit}종목 조회 중...")

    get_top_losers(market, limit)