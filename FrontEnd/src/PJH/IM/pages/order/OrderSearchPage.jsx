import React, { useState, useEffect, useMemo } from "react";
import OrderListPanel from "../../components/order/OrderSearch/OrderListPanel";
import OrderDetailPanel from "../../components/order/OrderSearch/OrderDetailPanel";
import "./OrderSearchPage.css";
import axios from "axios";
function OrderSearchPage() {
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [isLoading, setIsLoading] = useState(true);

  const handleDeleteOrder = async (orderIdToDelete) => {
    if (window.confirm("정말로 주문을 삭제하시겠습니까?")) {
      try {
        const response = await axios.delete(`/api/orders/${orderIdToDelete}`);

        if (response.status === 204) {
          alert("선택된 주문서가 성공적으로 삭제되었습니다.");

          setOrders(
            orders.filter((order) => order.orderId !== orderIdToDelete)
          );

          // 2. 현재 선택된 주문이 삭제된 주문이라면, 선택을 해제합니다.
          if (selectedOrder?.orderId === orderIdToDelete) {
            setSelectedOrder(null);
          }
        } else {
          throw new Error("삭제에 실패했습니다.");
        }
      } catch (err) {
        alert("주문서 삭제 중 오류가 발생했습니다.");
        console.error(err);
      }
    }
  };

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await fetch("/api/orders", {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || "서버에서 오류가 발생했습니다.");
        }
        //console.log("response: ", response);
        const orders = await response.json();
        setOrders(orders);
        if (orders.length > 0) {
          setSelectedOrder(orders[0]);
        }
      } catch (err) {
        console.log(err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrders();
  }, []);

  const filteredOrders = useMemo(() => {
    console.log("orders: ", orders);
    return orders
      .filter(
        (order) =>
          (statusFilter === "all" || order.status === statusFilter) &&
          (searchTerm === "" ||
            order.customer.customer_name
              .toLowerCase()
              .includes(searchTerm.toLowerCase()) ||
            String(order.orderId).includes(searchTerm))
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
            <OrderDetailPanel
              order={selectedOrder}
              onDeleteOrder={handleDeleteOrder}
            />
          </div>
        </main>
      </div>
    </div>
  );
}

export default OrderSearchPage;
