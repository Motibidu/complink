import React, { useState, useEffect } from "react";
import axios from "axios";
const SalesEntryForm = ({ order, onSubmit }) => {
  if (!order) return null;

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

  return (
    <div className="card mt-4">
      <div className="card-header">
        <h4 className="mb-0">주문서 정보 (주문번호: {order.orderId})</h4>
      </div>
      <div className="card-body">
        <div className="row g-3 mb-4">
          <div className="col-md-6">
            <strong>거래처:</strong> {order.customer.customerName}
          </div>
          <div className="col-md-6">
            <strong>담당자:</strong> {order.manager.managerName}
          </div>
          <div className="col-md-6">
            <strong>주문일:</strong> {order.orderDate}
          </div>
          <div className="col-md-6">
            <strong>납기일:</strong> {order.deliveryDate}
          </div>
        </div>

        <h5>주문 품목</h5>
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>품목명</th>
                <th className="text-end">수량</th>
                <th className="text-end">단가</th>
                <th className="text-end">합계</th>
              </tr>
            </thead>
            <tbody>
              {order.items.map((item) => (
                <tr key={item.orderItemId}>
                  <td>{item.itemName}</td>
                  <td className="text-end">{item.quantity.toLocaleString()}</td>
                  <td className="text-end">
                    {item.unitPrice.toLocaleString()}원
                  </td>
                  <td className="text-end">
                    {item.totalPrice.toLocaleString()}원
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr className="fw-bold">
                <td colSpan="3" className="text-end">
                  총 공급가액:
                </td>
                <td className="text-end">
                  {order.totalAmount.toLocaleString()}원
                </td>
              </tr>
              <tr className="fw-bold">
                <td colSpan="3" className="text-end">
                  부가세:
                </td>
                <td className="text-end">
                  {order.vatAmount.toLocaleString()}원
                </td>
              </tr>
              <tr className="fw-bold fs-5">
                <td colSpan="3" className="text-end">
                  최종 합계:
                </td>
                <td className="text-end">
                  {order.grandAmount.toLocaleString()}원
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
      <div className="card-footer text-end">
        <button
          className="btn btn-success"
          onClick={handleSubmit}
          disabled={isSubmitting}
        >
          {isSubmitting ? "저장 중..." : "판매입력 저장"}
        </button>
      </div>
    </div>
  );
};

export default SalesEntryForm;
