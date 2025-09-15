import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import ReCAPTCHA from 'react-google-recaptcha'; // 1. ReCAPTCHA 컴포넌트 import
import './SignUp.css';

function SignUp() {
    const [formData, setFormData] = useState({
        email: '',
        username: '',
        password: '',
        passwordConfirm: '',
        name: '',
        tel: '',
        address: '',
    });

    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [recaptchaToken, setRecaptchaToken] = useState(null); // 2. reCAPTCHA 토큰을 저장할 상태 추가
    const navigate = useNavigate();

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (formData.password !== formData.passwordConfirm) {
            setErrorMessage("비밀번호가 일치하지 않습니다.");
            return;
        }

        // 3. reCAPTCHA를 통과했는지 확인
        if (!recaptchaToken) {
            setErrorMessage("reCAPTCHA를 완료해주세요.");
            return;
        }

        setErrorMessage('');
        setSuccessMessage('');

        // const { passwordConfirm, ...userData } = formData;
        
        try {
            // 4. 서버로 보낼 데이터에 reCAPTCHA 토큰을 포함시킴
            const response = await axios.post('/api/signup', {
                 ...formData, 
                recaptchaToken: recaptchaToken, // 서버로 토큰 전송
            });

            if (response.status === 201 || response.status === 200) {
                setSuccessMessage("회원가입이 성공적으로 완료되었습니다! 잠시 후 로그인 페이지로 이동합니다.");
                setTimeout(() => navigate('/login'), 2000);
            }
        } catch (error) {
            const serverErrorMessage = error.response?.data?.message || "회원가입 중 오류가 발생했습니다.";
            setErrorMessage(serverErrorMessage);
            console.error('회원가입 요청 에러:', error);
        }
    };

    return (
        <div className="signup-container"> 
            <h2>회원가입</h2>
            <form onSubmit={handleSubmit}>
                {/* ... 다른 input 필드들은 동일 ... */}
                <div>
                    <input type="email" name="email" value={formData.email} onChange={handleChange} placeholder="이메일" required />
                </div>
                <div>
                    <input type="text" name="username" value={formData.username} onChange={handleChange} placeholder="아이디" required />
                </div>
                <div>
                    <input type="password" name="password" value={formData.password} onChange={handleChange} placeholder="비밀번호" required />
                </div>
                <div>
                    <input type="password" name="passwordConfirm" value={formData.passwordConfirm} onChange={handleChange} placeholder="비밀번호 확인" required />
                </div>               
                <div>
                    <input type="text" name="name" value={formData.name} onChange={handleChange} placeholder="이름" required />
                </div>
                <div>
                    <input type="tel" name="tel" value={formData.tel} onChange={handleChange} placeholder="전화번호" required />
                </div>
                <div>
                    <input type="text" name="address" value={formData.address} onChange={handleChange} placeholder="주소" required />
                </div>

                {/* 5. ReCAPTCHA 컴포넌트 추가 */}
                <div style={{ margin: '20px 0', display: 'flex', justifyContent: 'center' }}>
                    <ReCAPTCHA
                        sitekey="6LdEUJYrAAAAAKyv4NfvYcWJcPAEMnZ6Gz4jCm87"
                        onChange={(token) => setRecaptchaToken(token)}
                        onExpired={() => setRecaptchaToken(null)}
                    />
                </div>

                <button type="submit">가입하기</button>
            </form>
            {successMessage && <p className="success-message">{successMessage}</p>}
            {errorMessage && <p className="error-message">{errorMessage}</p>}
        </div>
    );
}

export default SignUp;
