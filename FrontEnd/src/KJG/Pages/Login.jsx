import React, { useState } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import './Login.css';

function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [isError, setIsError] = useState(false);

    // ▼▼▼ 이 함수가 없어서 발생한 에러입니다. 다시 추가합니다. ▼▼▼
    const handleLogin = async (e) => {
        e.preventDefault();

        if (!username || !password) {
            setMessage('아이디와 비밀번호를 모두 입력해주세요.');
            setIsError(true);
            return;
        }

        const formData = new URLSearchParams();
        formData.append('username', username);
        formData.append('password', password);

        try {
            axios.defaults.withCredentials = true;
            const response = await axios.post('/api/login', formData, {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            });

            if (response.status === 200) {
                setMessage('로그인 성공!');
                setIsError(false);
                // 성공 시, 1초 후에 메인 페이지(홈)으로 이동합니다.
                setTimeout(() => {
                    window.location.href = '/'; // 필요에 따라 다른 경로로 변경 가능
                }, 1000);
            }
        } catch (error) {
            // Spring Security의 기본 실패 메시지는 401 Unauthorized 입니다.
            if (error.response && error.response.status === 401) {
                setMessage('아이디 또는 비밀번호가 올바르지 않습니다.');
            } else {
                setMessage('로그인 중 오류가 발생했습니다. 다시 시도해주세요.');
            }
            setIsError(true);
            console.error('로그인 요청 에러:', error);
        }
    };
    // ▲▲▲ 여기까지가 handleLogin 함수입니다 ▲▲▲

    return (
        <div className="login-container">
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
                    <p className={`login-message ${isError ? 'error' : 'success'}`}>
                        {message}
                    </p>
                )}
            </form>
            
            {/* 아이디 찾기 링크가 포함된 부분입니다. */}
            <div className="extra-links">
                <Link to="/find-id">아이디 찾기</Link>
                <span>|</span>
                <Link to="/signup">회원가입</Link>
            </div>
        </div>
    );
}

export default Login;