import React, { useState } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import './FindPassword.css'; // 아래에서 생성할 CSS 파일을 import 합니다.

function FindPassword() {
    // 사용자 입력을 위한 상태
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [code, setCode] = useState('');
    
    // UI 흐름 제어를 위한 상태 (1: 정보 입력, 2: 인증번호 입력, 3: 완료)
    const [currentStep, setCurrentStep] = useState(1);
    
    // 사용자에게 메시지를 보여주기 위한 상태
    const [message, setMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false); // 로딩 중 버튼 비활성화

    // 1단계: 인증번호 발송 API 호출 함수
    const handleSendCode = async (e) => {
        e.preventDefault();
        if (!username || !email) {
            setErrorMessage('아이디와 이메일을 모두 입력해주세요.');
            return;
        }
        setIsLoading(true);
        setErrorMessage('');
        setMessage('');

        try {
            // 백엔드의 /api/find-password/send-mail 엔드포인트로 요청
            const response = await axios.post('/api/find-password/send-mail', { username, email });
            setMessage(response.data.message);
            setCurrentStep(2); // 성공 시, 2단계로 이동
        } catch (error) {
            setErrorMessage(error.response?.data?.message || '오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    // 2단계: 임시 비밀번호 재설정 API 호출 함수
    const handleResetPassword = async (e) => {
        e.preventDefault();
        if (!code) {
            setErrorMessage('인증번호를 입력해주세요.');
            return;
        }
        setIsLoading(true);
        setErrorMessage('');
        setMessage('');

        try {
            // 백엔드의 /api/find-password/reset 엔드포인트로 요청
            const response = await axios.post('/api/find-password/reset', { username, email, code });
            setMessage(response.data.message);
            setCurrentStep(3); // 성공 시, 완료 단계로 이동
        } catch (error) {
            setErrorMessage(error.response?.data?.message || '인증 중 오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    // 현재 단계에 맞는 화면을 그려주는 함수
    const renderStepContent = () => {
        switch (currentStep) {
            case 1:
                return (
                    <form onSubmit={handleSendCode}>
                        <h2>비밀번호 찾기</h2>
                        <p className="info-text">가입 시 사용한 아이디와 이메일 주소를 입력해주세요.</p>
                        <div className="input-group">
                            <input type="text" placeholder="아이디" value={username} onChange={e => setUsername(e.target.value)} required />
                        </div>
                        <div className="input-group">
                            <input type="email" placeholder="이메일" value={email} onChange={e => setEmail(e.target.value)} required />
                        </div>
                        <button type="submit" disabled={isLoading}>{isLoading ? '전송 중...' : '인증번호 받기'}</button>
                    </form>
                );
            case 2:
                return (
                    <form onSubmit={handleResetPassword}>
                        <h2>인증번호 입력</h2>
                        <p className="info-text"><strong>{email}</strong>(으)로 발송된 인증번호를 입력하세요.</p>
                        <div className="input-group">
                            <input type="text" placeholder="인증번호 6자리" value={code} onChange={e => setCode(e.target.value)} maxLength="6" required />
                        </div>
                        <button type="submit" disabled={isLoading}>{isLoading ? '확인 중...' : '비밀번호 재설정'}</button>
                    </form>
                );
            case 3:
                return (
                    <div className="result-container">
                        <h2>재설정 완료</h2>
                        <p className="info-text success-text">{message}</p>
                        <p>로그인 후, 보안을 위해 반드시 비밀번호를 변경해주세요.</p>
                        <Link to="/login" className="go-to-login-btn">로그인 페이지로 이동</Link>
                    </div>
                );
            default: return null;
        }
    };

    return (
        <div className="find-password-container">
            {renderStepContent()}
            {/* 1, 2단계에서만 메시지 표시 */}
            {message && currentStep < 3 && <p className="message-text success">{message}</p>}
            {errorMessage && <p className="message-text error">{errorMessage}</p>}
            
            {/* 1, 2단계에서만 '돌아가기' 링크 표시 */}
            {currentStep < 3 && (
                <div className="back-link">
                    <Link to="/login">로그인 페이지로 돌아가기</Link>
                </div>
            )}
        </div>
    );
}

export default FindPassword;