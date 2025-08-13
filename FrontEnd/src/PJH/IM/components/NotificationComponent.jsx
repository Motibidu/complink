import { useState, useEffect } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const NotificationComponent = () => {
  const [notification, setNotification] = useState("");

  useEffect(() => {
    // 1. STOMP 클라이언트 생성
    const client = new Client({
      // SockJS를 사용하여 웹소켓 연결
      webSocketFactory: () => new SockJS("/ws"), // Spring Boot 서버 주소
      connectHeaders: {
        // 필요한 경우 인증 헤더 추가
      },
      debug: (str) => {
        console.log(str);
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

        // 5초 후에 알림 메시지 숨기기
        setTimeout(() => {
          setNotification("");
        }, 5000);
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
  }, []); // 빈 배열을 전달하여 컴포넌트가 처음 렌더링될 때만 실행

  // 알림 메시지가 있을 경우 화면에 표시
  return (
    <div>
      {notification && (
        <div
          style={{
            position: "fixed",
            top: "20px",
            right: "20px",
            backgroundColor: "#4CAF50",
            color: "white",
            padding: "15px",
            borderRadius: "5px",
            zIndex: 1000,
          }}
        >
          {notification}
        </div>
      )}
    </div>
  );
};

export default NotificationComponent;
