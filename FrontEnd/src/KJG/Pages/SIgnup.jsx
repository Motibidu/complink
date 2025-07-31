import React, { useState } from 'react';
import './SignUp.css'; // CSS 파일 임포트

function SignUp() {
    // ... (내부 로직은 동일)
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

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData(prevFormData => ({
            ...prevFormData,
            [name]: value
        }));
    };

    const handleSubmit = (event) => {
        event.preventDefault();

        if (formData.password !== formData.passwordConfirm) {
            setErrorMessage("비밀번호가 일치하지 않습니다.");
            return;
        }

        setErrorMessage('');
        setSuccessMessage('');
        
        // ... fetch 로직 ...
    };


    return (
        <div className="signup-container"> 
            <h2>회원가입</h2>
            <form onSubmit={handleSubmit}>
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
                <button type="submit">가입하기</button>
            </form>
            {successMessage && <p className="success-message">{successMessage}</p>}
            {errorMessage && <p className="error-message">{errorMessage}</p>}
        </div>
    );
}

export default SignUp;