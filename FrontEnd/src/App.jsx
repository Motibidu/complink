import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import SignUp from './KJG/Pages/SIgnup';
import Login from './KJG/Pages/Login';

function App() {
  return (
    <BrowserRouter>
      <div className="App">
        <Routes>
          {/* 기본 경로 접속 시 /login으로 자동 이동 */}
          <Route path="/" element={<Navigate replace to="/login" />} />
          
          {/* 로그인 페이지 경로 */}
          <Route path="/login" element={
            <>
              <Login />
            </>
          } />

          {/* 회원가입 페이지 경로 */}
          <Route path="/signup" element={<SignUp />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;