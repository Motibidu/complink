import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import SignUp from './KJG/Pages/SignUp';
import Login from './KJG/Pages/Login';
import FindId from './KJG/Pages/FindId'; // 1. FindId 컴포넌트 import

function App() {
  return (
    <BrowserRouter>
      <div className="App">
        <Routes>
          {/* ... (기존 / 및 /login 경로) ... */}
          <Route path="/" element={<Navigate replace to="/login" />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          
          {/* 2. 아이디 찾기 페이지 경로 추가 */}
          <Route path="/find-id" element={<FindId />} />

        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;