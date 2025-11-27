import React, { createContext, useState, useContext, useEffect } from "react";
import axios from "axios";

// 1. Context 객체 생성
const AuthContext = createContext();

// 2. Context를 사용할 커스텀 Hook 생성
export const useAuth = () => {
  return useContext(AuthContext);
};

// 3. 상태와 함수를 제공할 Provider 컴포넌트 생성
export const AuthProvider = ({ children }) => {
  // 로그인 상태를 저장할 state (기본값은 false)
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userRole, setUserRole] = useState("");
  const [loading, setLoading] = useState(true);
  axios.defaults.withCredentials = true;

  const checkLoginStatus = async () => {
    try {
      // 1. 로그인 여부 확인 (예: 세션 체크 또는 토큰 체크)
      // (백엔드 API에 따라 경로는 다를 수 있음)
      // 예: GET /api/auth/status 또는 기존의 userRole API 활용
      const response = await axios.get("/api/users/userRole");
      console.log("AuthProviderResp: ", response);

      if (response.status === 200 && response.data) {
        setIsLoggedIn(true);
        setUserRole(String(response.data)); // ⭐️ 역할 저장!
        console.log("AuthContext: 권한 확인됨 ->", response.data);
      } else {
        // 로그인이 안 된 경우
        setIsLoggedIn(false);
        setUserRole("");
      }
    } catch (error) {
      console.error("로그인 상태 확인 실패:", error);
      setIsLoggedIn(false);
      setUserRole("");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkLoginStatus();
  }, []);

  // 로그인 함수

  // 로그아웃 함수
  const logout = async () => {
    try {
      await axios.post("/api/logout");
      setIsLoggedIn(false);
      setUserRole("");
      window.location.reload();
    } catch (err) {
      console.log("로그아웃 에러:", err);
      alert("로그아웃 중 오류가 발생했습니다.");
    }
  };

  // 공유할 값들
  const value = {
    isLoggedIn,
    logout,
    loading,
    userRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
