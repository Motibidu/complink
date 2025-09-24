import React, { createContext, useState, useContext, useEffect } from "react";

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

  useEffect(() => {
    const fetchIsLoggedIn = async () => {
      try {
        const response = await fetch("/api/users/isLoggedIn", {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        });
        if (response.ok) {
          const data = await response.json();
          setIsLoggedIn(data.isLoggedIn);
        } else {
          setIsLoggedIn(false);
        }
      } catch (err) {
        console.log("에러 발생:", err);
        setIsLoggedIn(false);
      }
    };
    fetchIsLoggedIn();
  }, []);

  // 로그인 함수
  const login = () => {
    // 실제로는 API 호출 후 성공 시 토큰 저장 및 상태 변경
    setIsLoggedIn(true);
  };

  // 로그아웃 함수
  const logout = () => {
    // 실제로는 저장된 토큰 삭제 및 상태 변경
    setIsLoggedIn(false);
  };

  // 공유할 값들
  const value = {
    isLoggedIn,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
