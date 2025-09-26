import React, { useState } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import './Login.css'; // Login.css를 import 합니다.

function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [isError, setIsError] = useState(false);

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
            // axios는 기본적으로 3xx 리다이렉션을 자동으로 따라갑니다.
            // 따라서 이곳에서 200 응답을 받았다면, 이미 리다이렉션이 완료된 후의 응답일 가능성이 높습니다.
            // 하지만 Spring Security의 formLoginSuccessHandler는 보통 응답 헤더에 Location을 포함하여
            // 클라이언트에게 리다이렉션을 지시하는 302 응답을 보냅니다.
            // axios가 302 응답을 받으면 그 응답 헤더의 Location을 따라갑니다.
            // 따라서 여기서는 `response.status`로 성공 여부를 확인하기보다는,
            // 리다이렉션이 발생하여 페이지가 전환되었는지 여부를 확인하는 것이 더 적절할 수 있습니다.
            const response = await axios.post('/api/login-process', formData, {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                // maxRedirects를 설정하지 않거나 5로 설정하여 axios가 리다이렉션을 자동으로 따라가도록 합니다.
                // maxRedirects: 5, // 기본값은 5입니다.
            });

            // 여기서는 302 리다이렉션이 axios에 의해 처리된 후의 최종 응답을 받게 됩니다.
            // 백엔드가 200 OK와 함께 어떤 데이터를 보냈을 수도 있습니다.
            // 또는, Spring Security의 successHandler가 클라이언트 리다이렉트를 담당하므로,
            // 이 `axios.post` 호출 자체는 사실상 리다이렉션 성공 후 더 이상 필요하지 않을 수 있습니다.
            // 여기서는 `response.status`로 성공을 확정하기 보다는, 백엔드에서 리다이렉션이 발생했는지를 확인합니다.

            // Spring Security의 successHandler가 `response.sendRedirect()`를 사용했다면
            // 브라우저는 자동으로 그 URL로 이동했을 것입니다.
            // 따라서, `axios.post`가 성공적으로 끝났다는 것은 (에러 없이 `try` 블록에 도달)
            // 백엔드가 302 리다이렉션 응답을 보냈고, 브라우저가 이를 처리했다는 의미입니다.
            // 이 시점에서는 이미 페이지가 전환되었거나 전환될 예정이므로,
            // 프론트엔드에서 강제로 `window.location.href`를 변경하는 것은 불필요하거나 중복일 수 있습니다.
            // 하지만 만약을 위해 성공 메시지를 표시하고 확실하게 이동시킵니다.

            setMessage('로그인 성공!');
            setIsError(false);
            
            // 백엔드에서 `response.sendRedirect`를 사용했으므로
            // 브라우저가 이미 Location 헤더를 읽고 리다이렉션을 시도했을 것입니다.
            // 이 `setTimeout`은 실제로 페이지 이동을 강제하는 역할을 합니다.
            // 만약 이미 페이지가 전환되고 있다면 이 코드는 실행되지 않거나 무의미할 수 있습니다.
            // 하지만 안전하게 한 번 더 지정해줍니다.
            setTimeout(() => {
                // Spring Security의 successHandler가 이미 리다이렉트 URL을 보냈으므로,
                // 이 URL은 백엔드의 `SecurityConfig.java`에 설정된 URL과 일치해야 합니다.
                window.location.href = '/'; // 또는 백엔드에서 설정한 리다이렉트 최종 경로
            }, 100); // 1초는 길 수 있으니 0.1초로 줄입니다.

        } catch (error) {
            // Spring Security의 formLogin은 인증 실패 시 401 Unauthorized를 반환합니다.
            // 그 외의 네트워크 오류, 서버 오류 등은 다른 상태 코드를 반환합니다.
            if (error.response) {
                if (error.response.status === 401) {
                    setMessage('아이디 또는 비밀번호가 올바르지 않습니다.');
                } else if (error.response.status === 502) {
                    setMessage('서버 연결 오류: 백엔드 서비스가 응답하지 않습니다.');
                } else {
                    setMessage(`로그인 중 오류가 발생했습니다. (상태 코드: ${error.response.status}) 다시 시도해주세요.`);
                }
            } else if (error.request) {
                // 요청은 보내졌지만 응답을 받지 못한 경우 (네트워크 오류 등)
                setMessage('네트워크 오류가 발생했습니다. 서버 연결 상태를 확인해주세요.');
            } else {
                // 요청 설정 중 오류 발생
                setMessage('로그인 요청 설정 중 오류가 발생했습니다.');
            }
            setIsError(true);
            console.error('로그인 요청 에러:', error);
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
                    <p className={`login-message ${isError ? 'error' : 'success'}`}>
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