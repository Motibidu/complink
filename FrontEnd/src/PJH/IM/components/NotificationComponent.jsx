import React, { useState, useEffect } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const NotificationComponent = () => {
  const [notification, setNotification] = useState("");
  // 💡 알림 창의 표시/숨김 상태를 관리하는 새로운 State
  const [isVisible, setIsVisible] = useState(false);

  // 💡 사용자가 '확인' 버튼을 눌렀을 때 실행될 함수
  const handleConfirm = () => {
    setIsVisible(false);
    // 알림 내용을 바로 지우지 않고, 창이 닫힌 후 다음 알림을 받을 준비를 위해 남겨둘 수도 있습니다.
    // 여기서는 명확하게 내용을 초기화합니다.
    setNotification(""); 
  };

  useEffect(() => {
    // 1. STOMP 클라이언트 생성
    const client = new Client({
      // SockJS를 사용하여 웹소켓 연결
      webSocketFactory: () => new SockJS("/ws"), // Spring Boot 서버 주소
      connectHeaders: {
        // 필요한 경우 인증 헤더 추가
      },
      debug: (str) => {
        // console.log(str); // 디버그 로그는 잠시 주석 처리
      },
      reconnectDelay: 5000, // 5초마다 재연결 시도
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    // 2. 연결 성공 시 콜백
    client.onConnect = (frame) => {
      console.log("Connected: " + frame);

      // "/topic/notifications" 토픽 구독 시작
      client.subscribe("/topic/notifications", (message) => {
        // 메시지 수신 시 알림 상태 업데이트
        console.log("Received message: " + message.body);
        setNotification(message.body);
        setIsVisible(true); // 💡 메시지를 받으면 알림 창을 띄웁니다.
        // 기존의 setTimeout 로직은 제거되었습니다.
      });
    };

    // 3. 연결 오류 시 콜백
    client.onStompError = (frame) => {
      console.error("Broker reported error: " + frame.headers["message"]);
      console.error("Additional details: " + frame.body);
    };

    // 4. 클라이언트 활성화 (연결 시작)
    client.activate();

    // 5. 컴포넌트 언마운트 시 연결 비활성화
    return () => {
      client.deactivate();
      console.log("Disconnected");
    };
  }, []); 

  // 알림 메시지가 있고 isVisible이 true일 경우에만 화면에 표시
  return (
    <div>
      {isVisible && notification && (
        <div
          style={{
            position: "fixed",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            backgroundColor: "#2c3e50", // 진한 네이비 배경
            color: "white",
            padding: "25px 35px",
            borderRadius: "12px",
            boxShadow: "0 8px 25px rgba(0, 0, 0, 0.4)", // 깊은 그림자
            fontSize: "1.3rem", 
            fontWeight: "500",
            textAlign: "center",
            zIndex: 1000,
            minWidth: "350px", 
            maxWidth: "90vw", // 화면 폭에 맞게 조정
            animation: "fadeIn 0.5s ease-out", // 애니메이션 효과 추가
          }}
        >
          <p style={{ margin: "0 0 15px 0" }}>
             🔔 {notification}
          </p>
          <button
            onClick={handleConfirm}
            style={{
              padding: "10px 20px",
              border: "none",
              borderRadius: "6px",
              backgroundColor: "#1abc9c", // 산뜻한 녹색 계열 버튼
              color: "white",
              fontSize: "1rem",
              cursor: "pointer",
              fontWeight: "bold",
              transition: "background-color 0.2s",
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#16a085'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#1abc9c'}
          >
            확인
          </button>
        </div>
      )}

      {/* 💡 CSS 애니메이션 스타일 추가: React 컴포넌트의 스타일 섹션에 포함 */}
      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: translate(-50%, -50%) scale(0.9); }
          to { opacity: 1; transform: translate(-50%, -50%) scale(1); }
        }
      `}</style>
    </div>
  );
};

export default NotificationComponent;
