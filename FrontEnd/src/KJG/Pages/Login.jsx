import React, { useState } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import './Login.css'; // 1. Login.css 파일을 import 합니다.

function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [isError, setIsError] = useState(false); // 2. 메시지가 에러인지 상태를 관리합니다.

    const handleLogin = async (e) => {
        e.preventDefault();

        if (!username || !password) {
            setMessage('아이디와 비밀번호를 모두 입력해주세요.');
            setIsError(true); // 에러 상태로 설정
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
                setIsError(false); // 성공 상태로 설정
                setTimeout(() => {
                    window.location.href = '/';
                }, 1000);
            }
        } catch (error) {
            setMessage('로그인 실패: 아이디 또는 비밀번호를 확인하세요.');
            setIsError(true); // 에러 상태로 설정
            console.error('로그인 요청 에러:', error);
        }
    };

    return (
        // 3. 전체를 .login-container div로 감싸줍니다.
        <div className="login-container">
            <form onSubmit={handleLogin}>
                <h2>PC Gear 로그인</h2>
                {/* 4. 각 input을 div로 감싸 클래스를 부여합니다. */}
                <div className="input-group">
                    <input
                        type="text"
                        placeholder="아이디"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />
                </div>
                <div className="input-group">
                    <input
                        type="password"
                        placeholder="비밀번호"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>
                <button type="submit">로그인</button>
                
                {/* 5. 메시지 표시에 동적 클래스를 적용합니다. */}
                {message && (
                    <p className={`login-message ${isError ? 'error' : 'success'}`}>
                        {message}
                    </p>
                )}
            </form>
            
            {/* 6. 회원가입 링크에 클래스를 적용합니다. */}
            <div className="signup-link">
                계정이 없으신가요? <Link to="/signup">회원가입</Link>
            </div>
        </div>
    );
}

export default Login;