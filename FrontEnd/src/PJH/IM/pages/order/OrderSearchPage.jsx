import React, { useState, useEffect, useMemo } from "react";
import OrderListPanel from "../../components/order/OrderSearch/OrderListPanel";
import OrderDetailPanel from "../../components/order/OrderSearch/OrderDetailPanel";
import { mockUsers, mockCustomers, mockOrders } from "../../datas/mockData";
import "./OrderSearchPage.css";
function OrderSearchPage() {
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    setTimeout(() => {
      const processedOrders = mockOrders.map((order) => ({
        ...order,
        customer: mockCustomers.find(
          (c) => c.customer_id === order.customer_id
        ),
        manager: mockUsers.find((u) => u.user_id === order.manager_id),
      }));
      setOrders(processedOrders);
      // 첫 번째 주문을 기본으로 선택
      if (processedOrders.length > 0) {
        setSelectedOrder(processedOrders[0]);
      }
      setIsLoading(false);
    }, 1000);
  }, []);

  const filteredOrders = useMemo(() => {
    return orders
      .filter(
        (order) =>
          (statusFilter === "all" || order.status === statusFilter) &&
          (searchTerm === "" ||
            order.customer.customer_name
              .toLowerCase()
              .includes(searchTerm.toLowerCase()) ||
            String(order.order_id).includes(searchTerm))
      )
      .sort((a, b) => new Date(b.order_date) - new Date(a.order_date));
  }, [orders, searchTerm, statusFilter]);

  return (
    <div className="bg-body-tertiary">
      <div className="container py-4 py-lg-5">
        <header className="mb-4">
          <h1 className="display-5 fw-bold">주문 조회</h1>
          <p className="text-muted">
            주문 내역을 검색하고 상세 정보를 확인합니다.
          </p>
        </header>

        <main className="row g-4">
          <div className="col-lg-4">
            <OrderListPanel
              orders={filteredOrders}
              selectedOrder={selectedOrder}
              onSelectOrder={setSelectedOrder}
              searchTerm={searchTerm}
              onSearch={setSearchTerm}
              statusFilter={statusFilter}
              onFilterChange={setStatusFilter}
              isLoading={isLoading}
            />
          </div>
          <div className="col-lg-8">
            <OrderDetailPanel order={selectedOrder} />
          </div>
        </main>
      </div>
    </div>
  );
}

export default OrderSearchPage;
