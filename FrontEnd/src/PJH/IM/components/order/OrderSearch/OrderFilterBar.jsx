import React from "react";

const OrderFilterBar = ({ condition, setCondition, onSearch }) => {
  const handleChange = (e) => {
    const { name, value } = e.target;
    setCondition((prev) => ({ ...prev, [name]: value }));
  };

  // 엔터키 검색 지원
  const handleKeyDown = (e) => {
    if (e.key === "Enter") onSearch();
  };

  return (
    <div className="row g-3 align-items-end">
      {/* 기간 검색 */}
      <div className="col-md-3">
        <label className="form-label small text-muted">주문 기간</label>
        <div className="input-group">
          <input
            type="date"
            className="form-control"
            name="startDate"
            value={condition.startDate}
            onChange={handleChange}
          />
          <span className="input-group-text">~</span>
          <input
            type="date"
            className="form-control"
            name="endDate"
            value={condition.endDate}
            onChange={handleChange}
          />
        </div>
      </div>

      {/* 상태 필터 */}
      <div className="col-md-2">
        <label className="form-label small text-muted">진행 상태</label>
        <select
          className="form-select"
          name="orderStatus"
          // 멀티 셀렉트 구현이 복잡하면 일단 단일 선택으로 시작
          value={condition.orderStatus}
          onChange={handleChange}
        >
          <option value="">전체</option>
          <option value="ORDER_RECEIVED">주문 접수</option>
          <option value="PAID">결제 완료</option>
          <option value="SHIPPING">배송 중</option>
          {/* ... */}
        </select>
      </div>

      {/* 키워드 검색 */}
      <div className="col-md-3">
        <label className="form-label small text-muted">통합 검색</label>
        <input
          type="text"
          className="form-control"
          name="keyword"
          placeholder="주문번호, 고객명, 담당자..."
          value={condition.keyword}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
        />
      </div>

      {/* 검색 버튼 */}
      <div className="col-md-2">
        <button className="btn btn-primary w-100" onClick={onSearch}>
          <i className="bi bi-search me-2"></i>조회
        </button>
      </div>
    </div>
  );
};

export default OrderFilterBar;