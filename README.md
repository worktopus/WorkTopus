# WorkTopus

1. oracle : CHAT_READ(채팅 상대가 읽엇는지), CHAT_MESSAGE(메시지 저장), AI_MEETING_SUMMARY(회의록 저장)

MeetingSummaryController
        ↓
MeetingSummaryService
        ↓
ChatService
        ↓
CHAT_MESSAGE
        ↓
project_2_group 메시지 조회
        ↓
GeminiClientService
        ↓
Gemini API
        ↓
JSON 요약
        ↓
MeetingSummaryResponse
        ↓
브라우저 반환



AI 회의요약 클릭
        ↓
Gemini 분석
        ↓
MeetingSummaryResponse
        ↓
팝업 표시
        ↓
[회의록 저장] 버튼
        ↓
POST /api/ai/meeting-summary/save
        ↓
MeetingSummaryEntity
        ↓
AI_MEETING_SUMMARY 저장





단체채팅방
   ↓
📋 회의록 기록 클릭
   ↓
현재 프로젝트의 저장된 회의록 목록 조회
   ↓
회의록 하나 클릭
   ↓
summaryId로 상세 조회
   ↓
기존 AI 회의요약 팝업 재사용


