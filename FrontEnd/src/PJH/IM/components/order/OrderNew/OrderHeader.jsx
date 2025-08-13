import React from "react";

const OrderHeader = ({ orderHeader, handleHeaderChange }) => {
  return (
    <div className="orderHeader">
      <h3 className="orderHeader__title">주문 기본 정보</h3>
      <div className="orderHeader__group">
        <label className="orderHeader__label">
          일자:
          <input
            className="orderHeader__input"
            type="date"
            name="orderDate"
            value={orderHeader.orderDate}
            onChange={handleHeaderChange}
          />
        </label>
        <label className="orderHeader__label">
          담당자:
          <input
            className="orderHeader__input"
            type="text"
            name="managerId"
            value={orderHeader.managerId}
            onChange={handleHeaderChange}
            placeholder="담당자Id"
          />
          <input
            className="orderHeader__input"
            type="text"
            name="managerName"
            value={orderHeader.managerName}
            onChange={handleHeaderChange}
            placeholder="담당자명"
          />
        </label>
        <label className="orderHeader__label">
          거래처:
          <input
            className="orderHeader__input"
            type="text"
            name="customerId"
            value={orderHeader.customerId}
            onChange={handleHeaderChange}
            placeholder="거래처명"
          />
          <input
            className="orderHeader__input"
            type="text"
            name="customerName"
            value={orderHeader.customerName}
            onChange={handleHeaderChange}
            placeholder="거래처명"
          />
        </label>
        <label className="orderHeader__label">
          납기일:
          <input
            className="orderHeader__input"
            type="date"
            name="deliveryDate"
            value={orderHeader.deliveryDate}
            onChange={handleHeaderChange}
          />
        </label>
      </div>
    </div>
  );
};

export default OrderHeader;
