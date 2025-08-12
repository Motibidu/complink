import React from "react";

const OrderHeader = ({ orderHeader, handleHeaderChange }) => {
  return (
    <div className="order-header">
      <h3 className="order-header__title">주문 기본 정보</h3>
      <div className="order-header__group">
        <label className="order-header__label">
          일자:
          <input
            className="order-header__input"
            type="date"
            name="orderDate"
            value={orderHeader.orderDate}
            onChange={handleHeaderChange}
          />
        </label>
        <label className="order-header__label">
          담당자:
          <input
            className="order-header__input"
            type="text"
            name="manager"
            value={orderHeader.manager}
            onChange={handleHeaderChange}
            placeholder="담당자명"
          />
        </label>
        <label className="order-header__label">
          거래처:
          <input
            className="order-header__input"
            type="text"
            name="client"
            value={orderHeader.client}
            onChange={handleHeaderChange}
            placeholder="거래처명"
          />
        </label>
        <label className="order-header__label">
          납기일:
          <input
            className="order-header__input"
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
