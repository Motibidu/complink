import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const ProtectedRoute = () => {
    // ⭐️ loading 상태를 추가로 가져옵니다. ⭐️
    const { isLoggedIn, loading } = useAuth(); 

    console.log("isLoggedIn: ", isLoggedIn, " | Loading:", loading);

    // ⭐️ 1. 로딩 중일 때는 아무것도 렌더링하지 않고 기다립니다. ⭐️
    if (loading) {
        // null을 리턴하거나 로딩 스피너 등을 표시하여 컴포넌트 렌더링을 지연시킵니다.
        return <div>인증 정보 확인 중...</div>; 
    }

    // 2. 로딩이 끝났는데 isLoggedIn이 false면 로그인 페이지로 이동합니다.
    if (!isLoggedIn) {
        return <Navigate to="/login" replace />;
    }

    // 3. 인증이 완료되면 요청한 페이지를 렌더링합니다.
    return <Outlet />;
};


export default ProtectedRoute;