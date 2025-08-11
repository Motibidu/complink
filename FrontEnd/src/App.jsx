import { Routes, Route } from "react-router-dom";
import Home from "./PJH/IM/pages/Home";
import Layout from "./PJH/IM/components/Layout";
import OrderFormPage from "./PJH/IM/pages/order/OrderFormPage";
import "./App.css";
import OrderSearchPage from "./PJH/IM/pages/order/OrderSearchPage";

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/orders/new" element={<OrderFormPage />} />
        <Route path="/orders/search" element={<OrderSearchPage />} />
      </Routes>
    </Layout>
  );
}

export default App;
