//<<<<<<< HEAD
import { Routes, Route } from "react-router-dom";
import Home from "./PJH/IM/pages/Home";
import Layout from "./PJH/IM/components/Layout";
import SignUp from "./KJG/Pages/SignUp";
import Login from "./KJG/Pages/Login";
import FindId from "./KJG/Pages/FindId";
import OrderFormPage from "./PJH/IM/pages/order/OrderFormPage";
import FindPassword from "./KJG/Pages/FindPassword";
import "./App.css";
import OrderSearchPage from "./PJH/IM/pages/order/OrderSearchPage";

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/find-id" element={<FindId />} />
        <Route path="/find-password" element={<FindPassword />} />
        <Route path="/orders/new" element={<OrderFormPage />} />
        <Route path="/orders/search" element={<OrderSearchPage />} />
      </Routes>
    </Layout>
  );
}

export default App;

// import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
// import './App.css';
// import SignUp from './KJG/Pages/SignUp';
// import Login from './KJG/Pages/Login';
// import FindId from './KJG/Pages/FindId';
// import FindPassword from './KJG/Pages/FindPassword'; // 1. import 추가

// function App() {
//   return (
//     <BrowserRouter>
//       <div className="App">
//         <Routes>
//           <Route path="/" element={<Navigate replace to="/login" />} />
//           <Route path="/login" element={<Login />} />
//           <Route path="/signup" element={<SignUp />} />
//           <Route path="/find-id" element={<FindId />} />
//           <Route path="/find-password" element={<FindPassword />} /> {/* 2. 라우트 추가 */}
//         </Routes>
//       </div>
//     </BrowserRouter>
//   );
// }

// export default App;
