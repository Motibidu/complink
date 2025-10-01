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
import InventoryStatus from "./PJH/IM/components/Inventory/InventoryStatus";
import ProtectedRoute from "./PJH/IM/components/ProtectedRoute";
import SellsSearchPage from "./PJH/IM/components/sells/SellsSearchPage";
import SellsEntryPage from "./PJH/IM/components/sells/SellsEntryPage";

function App() {
  return (
    <AuthProvider>
      <Layout>
        <Routes>
          {/* 공개 라우트 */}
          <Route path="/" element={<RegisterItemPage />} /> {/* 초기 페이지는 공개 */}
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/find-id" element={<FindId />} />
          <Route path="/find-password" element={<FindPassword />} />

          {/* 보호된 라우트들 */}
          <Route element={<ProtectedRoute />}> {/* ProtectedRoute로 감싸기 */}
            <Route path="/orders/new" element={<OrderFormPage />} />
            <Route path="/orders/search" element={<OrderSearchPage />} />
            <Route path="/registers/item" element={<RegisterItemPage />} />
            <Route path="/registers/customer" element={<RegisterCustomerPage />} />
            <Route path="/registers/manager" element={<RegisterManagerPage />} />
            <Route path="/inventory/status" element={<InventoryStatus />} />
            <Route path="/sells/new" element={<SellsEntryPage />} />
            <Route path="/sells/search" element={<SellsSearchPage />} />
          </Route>
        </Routes>
      </Layout>
    </AuthProvider>
  );
}

export default App;
