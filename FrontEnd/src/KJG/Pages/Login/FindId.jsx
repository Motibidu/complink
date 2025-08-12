import React, { useState } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import './FindId.css'; // 아래에서 생성할 CSS 파일을 import 합니다.

function FindId() {
    const [email, setEmail] = useState('');
    const [code, setCode] = useState('');
    const [currentStep, setCurrentStep] = useState(1); // 1: 이메일 입력, 2: 코드 입력, 3: 결과 표시
    
    const [message, setMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [foundUserId, setFoundUserId] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    // 1단계: 인증번호 발송 요청 핸들러
    const handleSendCode = async (e) => {
        e.preventDefault();
        if (!email) {
            setErrorMessage('이메일을 입력해주세요.');
            return;
        }
        setIsLoading(true);
        setErrorMessage('');
        setMessage('');

        try {
            // 백엔드의 /api/find/send-mail 엔드포인트로 요청
            const response = await axios.post('/api/find/send-mail', { email });
            setMessage(response.data.message);
            setCurrentStep(2); // 성공 시, 다음 단계로 이동
        } catch (error) {
            const serverError = error.response?.data?.message || '오류가 발생했습니다.';
            setErrorMessage(serverError);
        } finally {
            setIsLoading(false);
        }
    };

    // 2단계: 인증번호 확인 및 아이디 찾기 요청 핸들러
    const handleVerifyCode = async (e) => {
        e.preventDefault();
        if (!code) {
            setErrorMessage('인증번호를 입력해주세요.');
            return;
        }
        setIsLoading(true);
        setErrorMessage('');

        try {
            // 백엔드의 /api/find/verify-code 엔드포인트로 요청
            const response = await axios.post('/api/find/verify-code', { email, code });
            setFoundUserId(response.data.userId); // 마스킹된 아이디 저장
            setCurrentStep(3); // 성공 시, 결과 표시 단계로 이동
        } catch (error) {
            const serverError = error.response?.data?.message || '인증 중 오류가 발생했습니다.';
            setErrorMessage(serverError);
        } finally {
            setIsLoading(false);
        }
    };

    // 렌더링 함수
    const renderStep = () => {
        switch (currentStep) {
            case 1: // 이메일 입력 단계
                return (
                    <form onSubmit={handleSendCode}>
                        <h2>아이디 찾기</h2>
                        <div className="input-group">
                            <input
                                type="email"
                                placeholder="가입 시 사용한 이메일을 입력하세요"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>
                        <button type="submit" disabled={isLoading}>
                            {isLoading ? '전송 중...' : '인증번호 발송'}
                        </button>
                    </form>
                );
            case 2: // 인증번호 입력 단계
                return (
                    <form onSubmit={handleVerifyCode}>
                        <h2>인증번호 입력</h2>
                        <p className="info-text">
                            <strong>{email}</strong>로 발송된 인증번호 6자리를 입력해주세요.
                        </p>
                        <div className="input-group">
                            <input
                                type="text"
                                placeholder="인증번호 입력"
                                value={code}
                                onChange={(e) => setCode(e.target.value)}
                                maxLength="6"
                                required
                            />
                        </div>
                        <button type="submit" disabled={isLoading}>
                            {isLoading ? '확인 중...' : '아이디 찾기'}
                        </button>
                    </form>
                );
            case 3: // 결과 표시 단계
                return (
                    <div className="result-container">
                        <h2>아이디 찾기 완료</h2>
                        <p className="info-text">회원님의 아이디는 아래와 같습니다.</p>
                        <p className="found-id">{foundUserId}</p>
                        <Link to="/login" className="go-to-login-btn">로그인 하러가기</Link>
                    </div>
                );
            default:
                return null;
        }
    };

    return (
        <div className="findid-container">
            {renderStep()}
            {message && <p className="message-text success">{message}</p>}
            {errorMessage && <p className="message-text error">{errorMessage}</p>}
            {currentStep < 3 && (
                <div className="back-link">
                    <Link to="/login">로그인 페이지로 돌아가기</Link>
                </div>
            )}
        </div>
    );
}

export default FindId;