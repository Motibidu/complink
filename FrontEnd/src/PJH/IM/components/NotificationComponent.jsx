import React, { useState, useEffect } from "react";

const NotificationComponent = () => {
  const [notification, setNotification] = useState("");
  const [isVisible, setIsVisible] = useState(false);

  const handleConfirm = () => {
    setIsVisible(false);
    setNotification("");
  };

  useEffect(() => {
    // SSE 연결
    const eventSource = new EventSource("/api/notifications/subscribe");

    eventSource.addEventListener("notification", (event) => {
      console.log("Received notification: " + event.data);
      setNotification(event.data);
      setIsVisible(true);
    });

    eventSource.addEventListener("connect", (event) => {
      console.log("SSE connected: " + event.data);
    });

    eventSource.onerror = (error) => {
      console.error("SSE connection error:", error);
      // EventSource는 자동으로 재연결을 시도함
    };

    return () => {
      eventSource.close();
      console.log("SSE disconnected");
    };
  }, []);

  return (
    <div>
      {isVisible && notification && (
        <div
          style={{
            position: "fixed",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            backgroundColor: "#2c3e50",
            color: "white",
            padding: "25px 35px",
            borderRadius: "12px",
            boxShadow: "0 8px 25px rgba(0, 0, 0, 0.4)",
            fontSize: "1.3rem",
            fontWeight: "500",
            textAlign: "center",
            zIndex: 1000,
            minWidth: "350px",
            maxWidth: "90vw",
            animation: "fadeIn 0.5s ease-out",
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
              backgroundColor: "#1abc9c",
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
