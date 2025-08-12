import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import SignUp from './KJG/Pages/SignUp/SignUp';
import Login from './KJG/Pages/Login/Login';
import FindId from './KJG/Pages/Login/FindId';
import FindPassword from './KJG/Pages/Login/FindPassword'; // 1. import 추가

function App() {
  return (
    <BrowserRouter>
      <div className="App">
        <Routes>
          <Route path="/" element={<Navigate replace to="/login" />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/find-id" element={<FindId />} />
          <Route path="/find-password" element={<FindPassword />} /> {/* 2. 라우트 추가 */}
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;