import React, { useState, useEffect, useMemo, useCallback, use } from "react";
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

  const handleCancelOrder = async (orderIdToCancel) => {
    if (window.confirm("정말로 주문을 취소하시겠습니까?")) {
      try {
        // 1. API 경로도 일관되게 맞춤
        const response = await axios.post(
          `/api/orders/${orderIdToCancel}/cancel`
        );

        // 2. 204 대신 200 OK를 확인
        if (response.status === 200) {
          alert("선택된 주문서가 성공적으로 취소되었습니다.");

          // 3. (핵심) response.data로 받은 '최신 주문 객체'로 상태를 업데이트
          const canceledOrder = response.data;

          setOrders(
            orders.map((order) =>
              order.orderId === orderIdToCancel
                ? canceledOrder // 목록에서 해당 주문을 '최신 데이터'로 교체
                : order
            )
          );

          // 4. 현재 선택된 주문도 최신 데이터로 업데이트
          if (selectedOrder?.orderId === orderIdToCancel) {
            setSelectedOrder(canceledOrder);
          }
        } else {
          throw new Error("취소에 실패했습니다.");
        }
      } catch (err) {
        alert("주문서 취소 중 오류가 발생했습니다.");
        console.error(err);
      }
    }
  };

  const handleStatusUpdate = useCallback((orderId, newOrderData) => {
    setOrders((prevOrders) =>
      prevOrders.map((order) =>
        order.orderId === orderId ? newOrderData : order
      )
    );

    // 2. 'selectedOrder' 상태도 최신 데이터로 업데이트합니다.
    setSelectedOrder(newOrderData);
  }, []);

  useEffect(() => {
    const fetchOrders = async () => {
      setIsLoading(true);
      try {
        const response = await axios.get("/api/orders");
        const data = Array.isArray(response.data) ? response.data : [];
        setOrders(data);
        if (data.length > 0 && !selectedOrder) {
          setSelectedOrder(data[0]);
        }
      } catch (err) {
        console.error("주문 목록을 불러오는 데 실패했습니다.", err);
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
            order.customer.customerName
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
              handleCancelOrder={handleCancelOrder}
              onStatusUpdate={handleStatusUpdate}
            />
          </div>
        </main>
      </div>
    </div>
  );
}

export default OrderSearchPage;
