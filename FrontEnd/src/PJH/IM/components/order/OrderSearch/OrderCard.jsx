import React from "react";
import {
  getStatusBadgeVariant,
  formatCurrency,
} from "../../../utils/formatters";

const OrderCard = ({ order, isSelected, onSelect }) => {
  const badgeColors = getStatusBadgeVariant(order.status);

  return (
    <a
      href="#!"
      onClick={(e) => {
        e.preventDefault();
        onSelect(order);
      }}
      className={`list-group-item list-group-item-action order-card ${
        isSelected ? "active" : ""
      }`}
      aria-current={isSelected}
    >
      {/* Row 1: Customer Name */}
      <div className="order-card__customer-name">
        {order.customer.customerName}
      </div>

      {/* Row 2: Meta info (ID and Status) */}
      <div className="d-flex justify-content-between align-items-center mt-1">
        <span className="order-card__meta">주문번호: {order.orderId}</span>
        <span
          className={`badge rounded-pill ${badgeColors.bg} ${badgeColors.text}`}
        >
          {order.status}
        </span>
      </div>

      {/* Row 3: Date and Total (with a separator) */}
      <div className="d-flex justify-content-between align-items-end mt-2 pt-2 border-top">
        <small className="order-card__meta">주문일: {order.orderDate}</small>
        <strong className="order-card__total">
          {formatCurrency(order.grandAmount)}원
        </strong>
      </div>
    </a>
  );
};

export default OrderCard;
