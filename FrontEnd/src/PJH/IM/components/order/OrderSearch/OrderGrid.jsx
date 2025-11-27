import React from "react";
import { formatCurrency } from "../../../utils/formatters"; // 경로 맞춰주세요

const OrderGrid = ({ orders, loading, selectedId, onSelect }) => {
  if (loading) return <div className="text-center p-5">로딩 중...</div>;
  if (orders.length === 0)
    return (
      <div className="text-center p-5 bg-light rounded text-muted">
        검색 결과가 없습니다.
      </div>
    );

  return (
    <div className="card shadow-sm" style={{ minHeight: "600px" }}>
      <div className="table-responsive">
        <table className="table table-hover align-middle mb-0">
          <thead className="bg-light">
            <tr>
              <th>주문번호</th>
              <th>접수일</th>
              <th>고객명</th>
              <th>담당자</th>
              <th>상태</th>
              <th className="text-end">총 금액</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr
                key={order.orderId}
                onClick={() => onSelect(order)}
                style={{ cursor: "pointer" }}
                className={selectedId === order.orderId ? "table-active" : ""}
              >
                <td>{order.orderId}</td>
                <td>{order.orderDate}</td>
                <td>{order.customer ? order.customer.customerName : "(고객 정보 없음)"}</td>
                <td>{order.manager? order.manager.managerName : "(담당자 정보 없음)"}</td>
                <td>
                  {/* 뱃지 스타일링 (유틸 함수 활용 추천) */}
                  <span className="badge bg-secondary">
                    {order.orderStatusDesc}
                  </span>
                </td>
                <td className="text-end fw-bold">
                  {formatCurrency(order.grandAmount)}원
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default OrderGrid;