# WorkTopus

1. oracle : CHAT_READ(채팅 상대가 읽엇는지), CHAT_MESSAGE(메시지 저장), AI_MEETING_SUMMARY(회의록 저장)

MeetingSummaryController  -> MeetingSummaryService -> ChatService -> CHAT_MESSAGE -> project_2_group 메시지 조회 -> GeminiClientService -> Gemini API -> JSON 요약 -> MeetingSummaryResponse -> 브라우저 반환

AI 회의요약 클릭 -> Gemini 분석 -> MeetingSummaryResponse -> 팝업 표시 -> [회의록 저장] 버튼 -> POST /api/ai/meeting-summary/save -> MeetingSummaryEntity -> AI_MEETING_SUMMARY 저장

단체채팅방 -> 📋 회의록 기록 클릭 -> 현재 프로젝트의 저장된 회의록 목록 조회 -> 회의록 하나 클릭 -> summaryId로 상세 조회 -> 기존 AI 회의요약 팝업 재사용


YAML
server:
  port: 8080


spring:
  application:
    name: WorkTopus
  thymeleaf:
    cache: false
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true


  datasource:
    driver-class-name: oracle.jdbc.OracleDriver
    url: jdbc:oracle:thin:@worktopus_high?TNS_ADMIN=src/main/resources/wallet
    username: ADMIN
    password: 'WorkTopus1234!'
   # driver-class-name: oracle.jdbc.OracleDriver
   # url: jdbc:oracle:thin:@localhost:1521:xe
   # username: test
   # password: 1234

    hikari:
      maximum-pool-size: 2
      minimum-idle: 1
      connection-timeout: 30000
      idle-timeout: 300000
      max-lifetime: 600000

  jpa:
    database-platform: org.hibernate.dialect.OracleDialect

    hibernate:
      ddl-auto: update
      #ddl-auto: none

    show-sql: true
    properties:
      hibernate:
        format_sql: true

  profiles:
    include: secret

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: "임시구글아이디"       # ◀ 이 줄 추가
            client-secret: "임시구글암호"   # ◀ 이 줄 추가
            scope:
              - profile
              - email

          kakao:
            client-id: "임시카카오아이디"     # ◀ 이 줄 추가
            client-secret: "임시카카오암호" # ◀ 이 줄 추가
            provider: kakao
            client-authentication-method: none
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/kakao"
            client-name: Kakao
            scope:
              - profile_nickname

        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

gemini:
  api-key: ${GEMINI_API_KEY}
  model: gemini-3.5-flash

logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.orm.jdbc.bind: trace
