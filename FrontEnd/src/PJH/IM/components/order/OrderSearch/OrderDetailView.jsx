import React, { useState } from "react";
import {
  getStatusBadgeVariant,
  formatCurrency,
} from "../../../utils/formatters";
import axios from "axios";

const OrderDetailView = ({ order, onDeleteOrder, onSubmit }) => {
  const badgeColors = getStatusBadgeVariant(order.status);

  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async () => {
    setIsSubmitting(true);
    try {
      await onSubmit(order.orderId);
      alert("판매입력이 성공적으로 완료되었습니다.");
    } catch (error) {
      alert("판매입력 처리 중 오류가 발생했습니다.");
      console.error("Submission error:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const deleteOrder = async (orderId) => {
    if (window.confirm("정말로 주문을 삭제하시겠습니까?")) {
      try {
        const response = await axios.delete("/api/orders/" + orderId);
        if (response.status === 204) {
          alert("선택된 주문서가 성공적으로 삭제되었습니다.");
        }
      } catch (e) {
        alert("주문서 삭제에 실패했습니다.");
        console.error(e);
      }
    }
  };

  return (
    <div className="order-detail-view">
      <div className="pb-3 mb-3 order-detail-view__header d-flex align-items-center">
        <h2 className="h4 mb-0 fw-bold">주문 상세 정보</h2>
        <span
          className={`badge fs-6 rounded-pill ${badgeColors.bg} ${badgeColors.text}`}
        >
          {order.status}
        </span>
        {onDeleteOrder ? (
          <div className="ms-auto d-flex gap-2">
            <button
              className={`badge fs-6 rounded-pill ${badgeColors.text}`}
              onClick={() => onDeleteOrder(order.orderId)}
            >
              삭제
            </button>
          </div>
        ) : (
          ""
        )}
      </div>
      <p className="text-muted mb-4">주문번호: {order.orderId}</p>

      <div className="row g-4 mb-4">
        <div className="col-md-6">
          <p className="order-detail-view__info-label">거래처명</p>
          <p className="order-detail-view__info-value">
            {order.customer.customerName} ({order.customer.phoneNumber})
          </p>
        </div>
        <div className="col-md-6">
          <p className="order-detail-view__info-label">담당자</p>
          <p className="order-detail-view__info-value">
            {order.manager.managerName} ({order.manager.managerPhoneNumber})
          </p>
        </div>
        <div className="col-12">
          <p className="order-detail-view__info-label">주소</p>
          <p className="order-detail-view__info-value">
            {order.customer.customerAddress}
          </p>
        </div>
        <div className="col-md-6">
          <p className="order-detail-view__info-label">주문일</p>
          <p className="order-detail-view__info-value">{order.orderDate}</p>
        </div>
        <div className="col-md-6">
          <p className="order-detail-view__info-label">납기일</p>
          <p className="order-detail-view__info-value">{order.deliveryDate}</p>
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
              <tr key={item.orderItemId}>
                <td className="text-muted">{item.orderItemId}</td>
                <td className="fw-semibold">{item.itemName}</td>
                <td className="text-end">{item.quantity}</td>
                <td className="text-end">{formatCurrency(item.unitPrice)}원</td>
                <td className="text-end fw-bold">
                  {formatCurrency(item.totalPrice)}원
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
              <span>{formatCurrency(order.totalAmount)}원</span>
            </div>
            <div className="list-group-item d-flex justify-content-between">
              <span className="text-muted">부가세 (VAT)</span>
              <span>{formatCurrency(order.vatAmount)}원</span>
            </div>
            <div className="list-group-item d-flex justify-content-between fw-bold fs-5 order-detail-view__grand-total">
              <span>최종 결제 금액</span>
              <span>{formatCurrency(order.grandAmount)}원</span>
            </div>
          </div>
        </div>
      </div>
      {onSubmit ? (
        <div className="card-footer text-end">
          <button
            className="btn btn-success"
            onClick={handleSubmit}
            disabled={isSubmitting}
          >
            {isSubmitting ? "저장 중..." : "판매입력 저장"}
          </button>
        </div>
      ) : (
        ""
      )}
    </div>
  );
};

export default OrderDetailView;
