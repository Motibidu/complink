import React from "react";
import {
  getStatusBadgeVariant,
  formatCurrency,
} from "../../../utils/formatters";

const OrderDetailView = ({ order }) => {
  const badgeColors = getStatusBadgeVariant(order.status);

  return (
    <div className="order-detail-view">
      <div className="pb-3 mb-3 order-detail-view__header">
        <h2 className="h4 mb-0 fw-bold">주문 상세 정보</h2>
        <span
          className={`badge fs-6 rounded-pill ${badgeColors.bg} ${badgeColors.text}`}
        >
          {order.status}
        </span>
      </div>
      <p className="text-muted mb-4">주문번호: {order.order_id}</p>

      <div className="row g-4 mb-4">
        <div className="col-md-6">
          <p className="order-detail-view__info-label">고객명</p>
          <p className="order-detail-view__info-value">
            {order.customer.customer_name}
          </p>
        </div>
        <div className="col-md-6">
          <p className="order-detail-view__info-label">담당자</p>
          <p className="order-detail-view__info-value">
            {order.customer.contact_person} ({order.customer.phone_number})
          </p>
        </div>
        <div className="col-12">
          <p className="order-detail-view__info-label">주소</p>
          <p className="order-detail-view__info-value">
            {order.customer.address}
          </p>
        </div>
        <div className="col-md-4">
          <p className="order-detail-view__info-label">주문일</p>
          <p className="order-detail-view__info-value">{order.order_date}</p>
        </div>
        <div className="col-md-4">
          <p className="order-detail-view__info-label">납기일</p>
          <p className="order-detail-view__info-value">{order.delivery_date}</p>
        </div>
        <div className="col-md-4">
          <p className="order-detail-view__info-label">주문 담당 직원</p>
          <p className="order-detail-view__info-value">
            {order.manager.user_name}
          </p>
        </div>
      </div>

      <h4 className="h5 mb-3 fw-bold">주문 상품 목록</h4>
      <div className="table-responsive mb-4">
        <table className="table align-middle">
          <thead>
            <tr>
              <th>품번</th>
              <th>상품명</th>
              <th className="text-end">수량</th>
              <th className="text-end">단가</th>
              <th className="text-end">합계</th>
            </tr>
          </thead>
          <tbody>
            {order.items.map((item) => (
              <tr key={item.order_item_id}>
                <td className="text-muted">{item.part_number}</td>
                <td className="fw-semibold">{item.item_name}</td>
                <td className="text-end">{item.quantity}</td>
                <td className="text-end">
                  {formatCurrency(item.unit_price)}원
                </td>
                <td className="text-end fw-bold">
                  {formatCurrency(item.item_total)}원
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="row justify-content-end">
        <div className="col-md-7 col-lg-6">
          <div className="list-group list-group-flush order-detail-view__totals">
            <div className="list-group-item d-flex justify-content-between">
              <span className="text-muted">상품 총액</span>
              <span>{formatCurrency(order.total_amount)}원</span>
            </div>
            <div className="list-group-item d-flex justify-content-between">
              <span className="text-muted">부가세 (VAT)</span>
              <span>{formatCurrency(order.vat_amount)}원</span>
            </div>
            <div className="list-group-item d-flex justify-content-between fw-bold fs-5 order-detail-view__grand-total">
              <span>최종 결제 금액</span>
              <span>{formatCurrency(order.grand_total)}원</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrderDetailView;
