import { Routes, Route } from "react-router-dom";
import Home from "./PJH/IM/pages/Home";
import Layout from "./PJH/IM/components/Layout";
import OrderFormPage from "./PJH/IM/pages/OrderFormPage";
import "./App.css";

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/inventory" element={<Home />} />
        <Route path="/orders/new" element={<OrderFormPage />} />
      </Routes>
    </Layout>
  );
}

export default App;
