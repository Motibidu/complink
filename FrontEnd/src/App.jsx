
// import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import SignUp from './KJG/Pages/SignUp/SignUp';
import Login from './KJG/Pages/Login/Login';
import FindId from './KJG/Pages/Login/FindId';
import FindPassword from './KJG/Pages/Login/FindPassword'; // 1. import 추가

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
// =======
import { Routes, Route, Navigate } from "react-router-dom";
import Home from "./PJH/IM/pages/Home";
import Layout from "./PJH/IM/components/Layout";
//import OrderFormPage from "./PJH/IM/pages/OrderFormPage";
import "./App.css";

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/inventory" element={<Home />} />
        <Route path="/" element={<Navigate replace to="/login" />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/find-id" element={<FindId />} />
          <Route path="/find-password" element={<FindPassword />} />
        {/* <Route path="/orders/new" element={<OrderFormPage />} /> */}
      </Routes>
    </Layout>
  );
}

export default App;
// >>>>>>> a31356a20dc72affd9651492b66924a1e4cd5d5f
