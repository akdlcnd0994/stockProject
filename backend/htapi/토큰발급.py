#!/usr/bin/env python3
"""
OAuth 토큰 발급 스크립트
한국투자증권 API 인증 토큰을 발급받습니다.
"""
import requests
import json

# API 정보
APP_KEY = "PSc3l5swHONsfGybiK9JLVwYVNBwu7KiGBN6"
APP_SECRET = "TfaWrzgaxdXYzT5madBSzzETpxTynYdvivH291UtdVUVnWEDpJvVpbJmoegg2Bmec19gYyTMqk0UfP76NvdQcaFiUkOTeDum/wdznmW3YJ9MVU5ERVIN5MjagnesQyVThT4Ww5HhUUPfQ097nbQS/BodDuu1fBzX5EHZ5xDx28YQaqVvv6k="

def get_access_token():
    """Access Token 발급"""
    url = "https://openapi.koreainvestment.com:9443/oauth2/tokenP"
    
    headers = {
        "Content-Type": "application/json"
    }
    
    body = {
        "grant_type": "client_credentials",
        "appkey": APP_KEY,
        "appsecret": APP_SECRET
    }
    
    response = requests.post(url, headers=headers, data=json.dumps(body))
    
    if response.status_code == 200:
        token_data = response.json()
        return token_data["access_token"]
    else:
        print(f"토큰 발급 실패: {response.status_code}")
        print(response.text)
        return None

if __name__ == "__main__":
    token = get_access_token()
    if token:
        print(f"토큰 발급 성공")
        print(f"Access Token: {token[:20]}...")  # 보안을 위해 일부만 출력
        # 토큰을 파일에 저장
        with open("token.txt", "w") as f:
            f.write(token)
        print("토큰이 token.txt 파일에 저장되었습니다.")