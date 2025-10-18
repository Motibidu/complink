import React, { useState } from "react";
import axios from "axios";
import { Link } from "react-router-dom";
import "./Login.css"; // Login.css를 import 합니다.

function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!username || !password) {
      setMessage("아이디와 비밀번호를 모두 입력해주세요.");
      setIsError(true);
      return;
    }

    const formData = new URLSearchParams();
    formData.append("username", username);
    formData.append("password", password);

    try {
      axios.defaults.withCredentials = true;
      const response = await axios.post("/api/login", formData, {
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
      });

      setMessage("로그인 성공!");
      setIsError(false);

      setTimeout(() => {
        window.location.href = "/";
      }, 100);
    } catch (error) {
      setMessage(error.response.data.message);
      setIsError(true);
    }
  };

  return (
    <div className="login-container">
      {/* 1. 기존 아이디/비밀번호 로그인 폼 */}
      <form onSubmit={handleLogin}>
        <h2>PC Gear 로그인</h2>
        <div className="input-group">
          <input
            type="text"
            placeholder="아이디"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="input-group">
          <input
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit">로그인</button>

        {message && (
          <p className={`login-message ${isError ? "error" : "success"}`}>
            {message}
          </p>
        )}
      </form>

      {/* 2. 소셜 로그인 영역 */}
      {/* <div className="social-login-divider">
                <span>OR</span>
            </div> */}

      {/* 
              Google 로그인 버튼
              - href 경로는 백엔드 서버의 주소와 스프링 시큐리티의 약속된 경로를 따릅니다.
              - /oauth2/authorization/{provider} 형식이므로 google을 사용합니다.
            */}
      {/* public 폴더에 google-logo.svg 파일을 넣어두면 이미지가 보입니다. */}
      {/* <a href="http://localhost:8080/oauth2/authorization/google" className="social-login-btn google">
                <img src="/google-logo.svg" alt="Google" /> 
                <span>Google 계정으로 로그인</span>
            </a> */}

      {/* 3. 기타 링크 영역 */}
      <div className="extra-links">
        <Link to="/find-id">아이디 찾기</Link>
        <span>|</span>
        <Link to="/find-password">비밀번호 찾기</Link>
        <span>|</span>
        <Link to="/signup">회원가입</Link>
      </div>
    </div>
  );
}

export default Login;
