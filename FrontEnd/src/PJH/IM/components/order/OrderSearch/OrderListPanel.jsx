import React from "react";
import OrderCard from "./OrderCard";

const OrderListPanel = ({
  orders,
  selectedOrder,
  onSelectOrder,
  isLoading,
  searchTerm,
  onSearch,
  statusFilter,
  onFilterChange,
}) => (
  <div className="custom-panel">
    <div className="p-3">
      {/* CHANGED: Use Bootstrap grid to place controls side-by-side */}
      <div className="row g-2">
        <div className="col-md-7">
          <div className="input-group">
            <span className="input-group-text bg-light border-light">
              <i className="bi bi-search"></i>
            </span>
            <input
              type="text"
              className="form-control bg-light border-light"
              placeholder="검색..."
              value={searchTerm}
              onChange={(e) => onSearch(e.target.value)}
            />
          </div>
        </div>
        <div className="col-md-5">
          <select
            className="form-select"
            value={statusFilter}
            onChange={(e) => onFilterChange(e.target.value)}
          >
            <option value="all">전체 상태</option>
            <option value="접수">접수</option>
            <option value="출고대기">출고대기</option>
            <option value="출고완료">출고완료</option>
            <option value="재고부족">재고부족</option>
            <option value="취소">취소</option>
          </select>
        </div>
      </div>
    </div>
    <div className="list-group list-group-flush order-list-panel__list">
      {isLoading ? (
        <div className="d-flex justify-content-center align-items-center h-100">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      ) : orders.length > 0 ? (
        orders.map((order) => (
          <OrderCard
            key={order.orderId}
            order={order}
            isSelected={selectedOrder?.orderId === order.orderId}
            onSelect={onSelectOrder}
          />
        ))
      ) : (
        <p className="text-center text-muted p-5">
          조건에 맞는 주문이 없습니다.
        </p>
      )}
    </div>
  </div>
);

export default OrderListPanel;
