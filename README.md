# 🎉깔깔-개발자-놀이터🎉 - SLACK BOT  

**개발자들을 위한 유머, 팁, 그리고 음악 추천 봇!**    

Slack을 통해 랜덤으로 
 
🤖 AI가 생성한 시니어 개발자가 들려주는 개발 팁과 개발자 이미지   
😄 개발자 유머 및 개발자 짤방  
🎵 코딩할 때 들을 수 있는 음악을 추천해줍니다.    

_"오늘도 코드에 치여 사는 AIBE 동료들에게..." _

---


## 🚀 기술 스택  
![Java](https://img.shields.io/badge/Java-007396?style=flat&logo=OpenJDK&logoColor=white) 
![Slack API](https://img.shields.io/badge/Slack-4A154B?style=flat&logo=Slack&logoColor=white) 
![HTTP Client](https://img.shields.io/badge/Java%20HTTP%20Client-007396?style=flat&logo=Java&logoColor=white) 
![AI API](https://img.shields.io/badge/AI%20API-FF6F00?style=flat)  

---

## 🎯 주요 기능  
✅ **랜덤 개발자 유머 및 짤 제공** → `JOKES`와 `IMGS` 환경변수에 저장된 유머 중 랜덤 전송 🃏  
✅ **AI가 생성한 개발 팁과 이미지** → 시니어가 들려주는 개발 팁과 동물 개발자 이미지를 AI가 생 🎨  
✅ **코딩할 때 들을 음악 추천** → `MUSICS` 환경변수에 저장된 플레이리스트 중 랜덤 추천 🎵  
✅ **Slack Webhook을 통해 자동 전송** → Slack에서 바로 확인 가능 💬  

---

## 📦 환경 변수 설정  
이 프로그램을 실행하려면 다음과 같은 환경 변수를 설정해야 합니다.  

| 환경 변수 | 설명 |
|-----------|------|
| `SLACK_WEBHOOK_URL` | Slack Webhook URL |
| `LLM_PROMPT` | AI에게 제공할 개발 팁 프롬프트 |
| `LLM2_IMG_TEMPLATE` | AI 이미지 생성 템플릿 |
| `LLM_API_URL` | LLM API 엔드포인트 |
| `LLM_API_KEY` | LLM API 키 |
| `LLM2_API_URL` | AI 이미지 생성 API 엔드포인트 |
| `LLM2_API_KEY` | AI 이미지 생성 API 키 |
| `JOKES` | 쉼표(`,`)로 구분된 개발자 유머 리스트 |
| `IMGS` | 쉼표(`,`)로 구분된 개발 관련 이미지 리스트 |
| `MUSICS` | 쉼표(`,`)로 구분된 코딩할 때 들을 음악 리스트 |

---

## 📩 참여 방법
개발자 놀이터에 더 많은 `유머` / `짤` / `음악`을 추가하고 싶다면,
GitHub Issue에 새로운 콘텐츠를 제안해 주세요!

### 📌 유머 추가
개발자라면 공감할 수 있는 `재미있는 유머`를 제안해주세요.
"JOKES" 환경 변수에 추가됩니다.

### 📌 짤 추가
개발자 관련 `밈`이나 `짤` 추가하고 싶다면 알려주세요!
"IMGS" 환경 변수에 추가됩니다.

### 📌 음악 추가
코딩할 때 듣기 좋은` 플레이리스트`나 추천 `음악`이 있다면 추가해주세요!
"MUSICS" 환경 변수에 추가됩니다.
