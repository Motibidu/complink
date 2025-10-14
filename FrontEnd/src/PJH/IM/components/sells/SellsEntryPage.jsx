import React, { useState, useEffect } from "react";
import SalesEntryForm from "./SellsEntryForm";
import axios from "axios";
import OrderDetailView from "../order/OrderSearch/OrderDetailView";

function SellsEntryPage() {
  const [pendingOrders, setPendingOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // 판매입력 대기중인 주문서 목록을 불러오는 함수
  const fetchPendingOrders = async () => {
    setIsLoading(true);
    try {
      // API 엔드포인트는 실제 환경에 맞게 수정해야 합니다.
      const response = await axios.get("/api/orders?orderStatus=PAID");
      setPendingOrders(response.data);
    } catch (error) {
      console.error("주문서 목록을 불러오는 데 실패했습니다.", error);
    } finally {
      setIsLoading(false);
    }
  };

  // 모달에서 주문서를 선택했을 때 처리하는 함수
  const handleOrderSelect = (order) => {
    setSelectedOrder(order);
  };

  const handleSubmitSalesEntry = async (orderId) => {
    const payload = {
      orderId: selectedOrder.orderId,
      sellDate: new Date().toISOString(),
      customerId: selectedOrder.customer.customerId,
      customerName: selectedOrder.customer.customerName,
      managerId: selectedOrder.manager.managerId,
      managerName: selectedOrder.manager.managerName,
      paymentStatus: '미납',
      totalAmount: selectedOrder.totalAmount,
      vatAmount: selectedOrder.vatAmount,
      grandAmount: selectedOrder.grandAmount,
    };
    const response = await axios.post("/api/sells", payload);
    console.log(response.data);
    setSelectedOrder(null);
    fetchPendingOrders();
  };

  return (
    <div className="container py-4">
      <header className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="display-5 fw-bold">판매 입력</h1>
        <button
          className="btn btn-primary btn-lg"
          data-bs-toggle="modal"
          data-bs-target="#orderSelectModal"
          onClick={fetchPendingOrders}
        >
          주문서 불러오기
        </button>
      </header>

      <main>
        {selectedOrder ? (
          <>
            <div className="px-7 bg-white p-4 rounded shadow-sm">
              <OrderDetailView
                order={selectedOrder}
                onSubmit={handleSubmitSalesEntry}
              />
            </div>
          </>
        ) : (
          <div className="text-center text-muted p-5 border rounded">
            <p className="fs-4">
              '주문서 불러오기'를 클릭하여 판매입력할 주문서를 선택하세요.
            </p>
          </div>
        )}
      </main>

      {/* 주문서 선택 모달 */}
      <div
        className="modal fade"
        id="orderSelectModal"
        tabIndex="-1"
        aria-labelledby="modalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-lg modal-dialog-scrollable">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title" id="modalLabel">
                주문서 선택
              </h5>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>
            <div className="modal-body">
              {isLoading ? (
                <p>로딩 중...</p>
              ) : (
                <div className="list-group">
                  {pendingOrders.map((order) => (
                    <button
                      key={order.orderId}
                      type="button"
                      className="list-group-item list-group-item-action"
                      data-bs-dismiss="modal"
                      onClick={() => handleOrderSelect(order)}
                    >
                      <div className="d-flex w-100 justify-content-between">
                        <h6 className="mb-1">주문번호: {order.orderId}</h6>
                        <small>{order.orderDate}</small>
                      </div>
                      <p className="mb-1">
                        거래처: {order.customer.customerName}
                      </p>
                      <small>
                        총액: {order.grandAmount.toLocaleString()}원
                      </small>
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SellsEntryPage;
