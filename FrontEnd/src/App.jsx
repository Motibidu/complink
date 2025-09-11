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
import { AuthProvider } from "./PJH/IM/contexts/AuthContext";
import RegisterCustomerPage from "./PJH/IM/components/register/RegisterCustomerPage";
import RegisterItemPage from "./PJH/IM/components/register/RegisterItemPage";
import RegisterManagerPage from "./PJH/IM/components/register/RegisterManagerPage";

function App() {
  return (
    <AuthProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/find-id" element={<FindId />} />
          <Route path="/find-password" element={<FindPassword />} />
          <Route path="/orders/new" element={<OrderFormPage />} />
          <Route path="/orders/search" element={<OrderSearchPage />} />
          <Route path="/registers/item" element={<RegisterItemPage />} />
          <Route
            path="/registers/customer"
            element={<RegisterCustomerPage />}
          />
          <Route path="/registers/manager" element={<RegisterManagerPage />} />
        </Routes>
      </Layout>
    </AuthProvider>
  );
}

export default App;
