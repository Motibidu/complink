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
  const [loading, setLoading] = useState(true);
  axios.defaults.withCredentials= true;

  useEffect(() => {
    const fetchIsLoggedIn = async () => {
      try {
        const response = await axios.get("/api/users/isLoggedIn");
        setIsLoggedIn(response.data.isLoggedIn);

      } catch (err) {
        console.log("로그인 상태 확인 에러:", err);
        setIsLoggedIn(false);
      } finally {
        setLoading(false); // 요청 완료 후 로딩 상태 변경
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
  const logout = async() => {
    try{
      await axios.post("/api/logout");
      setIsLoggedIn(false);
      window.location.reload(); 
    }catch(err)
    {
        console.log("로그아웃 에러:", err);
        alert("로그아웃 중 오류가 발생했습니다.");
    }

    
  };

  // 공유할 값들
  const value = {
    isLoggedIn,
    login,
    logout,
    loading
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
