import { Routes, Route, Navigate } from "react-router-dom";
import Home from "./PJH/IM/pages/Home";
import Layout from "./PJH/IM/components/Layout";
import SignUp from "./KJG/Pages/SignUp/SignUp";
import Login from "./KJG/Pages/Login/Login";
import FindId from "./KJG/Pages/Login/FindId";
import OrderFormPage from "./PJH/IM/pages/order/OrderFormPage";
import FindPassword from "./KJG/Pages/Login/FindPassword";
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
