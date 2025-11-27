import React, { useState, useMemo, useEffect, useCallback } from "react";
import axios from "axios";
import qs from "qs";
import { Pagination } from "react-bootstrap";
import { IoReorderFourOutline } from "react-icons/io5";

// OrderHeader 컴포넌트
const OrderHeader = ({ orderHeader, handleHeaderChange }) => {
  // --- 모달 상태 관리 ---
  const [loading, setLoading] = useState(false);

  // [거래처 모달] 상태
  const [customerInputValue, setCustomerInputValue] = useState(""); // 1. 타이핑용
  const [customerSearchTerm, setCustomerSearchTerm] = useState(""); // 2. API 호출용
  const [customerCurrentPage, setCustomerCurrentPage] = useState(0);
  const [customerPageData, setCustomerPageData] = useState({
    content: [],
    totalPages: 0,
    number: 0,
    first: true,
    last: true,
  });

  // [담당자 모달] 상태
  const [managerInputValue, setManagerInputValue] = useState(""); // 1. 타이핑용
  const [managerSearchTerm, setManagerSearchTerm] = useState(""); // 2. API 호출용
  const [managerCurrentPage, setManagerCurrentPage] = useState(0);
  const [managerPageData, setManagerPageData] = useState({
    content: [],
    totalPages: 0,
    number: 0,
    first: true,
    last: true,
  });

  // --- API 호출 함수 ---

  // [거래처] API 호출
  const fetchCustomers = useCallback(async (pageToFetch, searchTerm) => {
    setLoading(true);
    try {
      const response = await axios.get("/api/customers", {
        params: {
          page: pageToFetch,
          size: 10,
          sort: "customerId,desc",
          search: searchTerm, // ⬅️ "확정된" 검색어 사용
        },
      });
      setCustomerPageData(response.data);
    } catch (err) {
      console.error("거래처 목록 로드 실패:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  // [담당자] API 호출
  const fetchManagers = useCallback(async (pageToFetch, searchTerm) => {
    setLoading(true);
    try {
      const response = await axios.get("/api/managers", {
        params: {
          page: pageToFetch,
          size: 10,
          sort: "id,desc",
          search: searchTerm, // ⬅️ "확정된" 검색어 사용
        },
      });
      console.log("response.data: ", response.data);
      setManagerPageData(response.data);
    } catch (err) {
      console.error("담당자 목록 로드 실패: ", err);
    } finally {
      setLoading(false);
    }
  }, []);

  // --- useEffect 훅 ---

  // [거래처] "확정된" 검색어(customerSearchTerm)나 페이지가 바뀔 때만 API 호출
  useEffect(() => {
    fetchCustomers(customerCurrentPage, customerSearchTerm);
  }, [customerCurrentPage, customerSearchTerm, fetchCustomers]);

  // [담당자] "확정된" 검색어(managerSearchTerm)나 페이지가 바뀔 때만 API 호출
  useEffect(() => {
    fetchManagers(managerCurrentPage, managerSearchTerm);
  }, [managerCurrentPage, managerSearchTerm, fetchManagers]);

  // --- 핸들러 함수 ---

  const handleCustomerSelect = (customer) => {
    handleHeaderChange({
      target: { name: "customerId", value: customer.customerId },
    });
    handleHeaderChange({
      target: { name: "customerName", value: customer.customerName },
    });
  };

  const handleManagerSelect = (manager) => {
    handleHeaderChange({
      target: { name: "managerId", value: manager.managerId },
    });
    handleHeaderChange({
      target: { name: "managerName", value: manager.managerName },
    });
  };

  // [거래처] 검색창 "타이핑" 핸들러
  const handleCustomerInputChange = (e) => {
    setCustomerInputValue(e.target.value);
  };

  // [거래처] "검색" 버튼 클릭 또는 Enter 핸들러
  const handleCustomerSearchSubmit = () => {
    setCustomerSearchTerm(customerInputValue); // API 호출 트리거
    setCustomerCurrentPage(0); // 1페이지로 리셋
  };

  // [담당자] 검색창 "타이핑" 핸들러
  const handleManagerInputChange = (e) => {
    setManagerInputValue(e.target.value);
  };

  // [담당자] "검색" 버튼 클릭 또는 Enter 핸들러
  const handleManagerSearchSubmit = () => {
    setManagerSearchTerm(managerInputValue); // API 호출 트리거
    setManagerCurrentPage(0); // 1페이지로 리셋
  };

  // [공용] 페이지네이션 UI 생성 헬퍼
  const createPaginationItems = (pageData, setCurrentPage) => {
    let pages = [];
    const maxPagesToShow = 5;
    let startPage = Math.max(
      0,
      pageData.number - Math.floor(maxPagesToShow / 2)
    );
    let endPage = Math.min(
      pageData.totalPages - 1,
      startPage + maxPagesToShow - 1
    );

    if (endPage - startPage + 1 < maxPagesToShow) {
      startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }

    for (let number = startPage; number <= endPage; number++) {
      pages.push(
        <Pagination.Item
          key={number}
          active={number === pageData.number}
          onClick={() => setCurrentPage(number)}
        >
          {number + 1}
        </Pagination.Item>
      );
    }
    return pages;
  };

  // --- 렌더링 ---
  return (
    <>
      {/* --- (주문 기본 정보 UI - 변경 없음) --- */}
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
            <span>
              담당자
              <IoReorderFourOutline
                className="orderHeader__list"
                size={25}
                data-bs-toggle="modal"
                data-bs-target="#managerListModal"
                readOnly
              />
            </span>
            <input
              className="orderHeader__input"
              type="text"
              name="managerId"
              value={orderHeader.managerId}
              onChange={handleHeaderChange}
              placeholder="담당자 코드"
              data-bs-toggle="modal"
              data-bs-target="#managerListModal"
              readOnly
            />
            <input
              className="orderHeader__input"
              type="text"
              name="managerName"
              value={orderHeader.managerName}
              onChange={handleHeaderChange}
              placeholder="담당자명"
              data-bs-toggle="modal"
              data-bs-target="#managerListModal"
              readOnly
            />
          </label>
          <label className="orderHeader__label">
            <span>
              거래처
              <IoReorderFourOutline
                className="orderHeader__list"
                size={25}
                data-bs-toggle="modal"
                data-bs-target="#customerListModal"
              />
            </span>
            <input
              className="orderHeader__input"
              type="text"
              name="customerId"
              value={orderHeader.customerId}
              onChange={handleHeaderChange}
              placeholder="거래처 코드"
              data-bs-toggle="modal"
              data-bs-target="#customerListModal"
              readOnly
            />
            <input
              className="orderHeader__input"
              type="text"
              name="customerName"
              value={orderHeader.customerName}
              onChange={handleHeaderChange}
              placeholder="거래처명"
              data-bs-toggle="modal"
              data-bs-target="#customerListModal"
              readOnly
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

      {/* --- [거래처 선택 모달] (검색 버튼 적용) --- */}
      <div
        className="modal fade"
        id="customerListModal"
        tabIndex="-1"
        aria-labelledby="customerModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-scrollable">
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5" id="customerModalLabel">
                거래처 선택
              </h1>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>

            <div className="modal-body">
              {/* [수정] 1. 검색창 + 검색 버튼 (Input Group) */}
              <div className="input-group mb-3">
                <input
                  type="text"
                  className="form-control"
                  placeholder="거래처 코드 또는 이름으로 검색..."
                  value={customerInputValue} // ⬅️ 타이핑용 state
                  onChange={handleCustomerInputChange} // ⬅️ 타이핑 핸들러
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      e.preventDefault();
                      handleCustomerSearchSubmit();
                    }
                  }} // ⬅️ Enter 키 핸들러
                />
                <button
                  className="btn btn-primary"
                  type="button"
                  onClick={handleCustomerSearchSubmit} // ⬅️ 검색 버튼 핸들러
                >
                  검색
                </button>
              </div>

              {/* 2. 로딩 상태 표시 */}
              {loading && (
                <div className="d-flex justify-content-center my-5">
                  <div className="spinner-border" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </div>
                </div>
              )}

              {/* 3. 데이터가 있을 때 List Group으로 표시 */}
              {!loading && customerPageData.content.length > 0 && (
                <div className="list-group customer-list">
                  {customerPageData.content.map((customer) => (
                    <button
                      type="button"
                      key={customer.customerId}
                      className="list-group-item list-group-item-action"
                      data-bs-dismiss="modal"
                      onClick={() => handleCustomerSelect(customer)}
                    >
                      <div className="d-flex w-100 justify-content-between">
                        <h6 className="mb-1 fw-bold">
                          {customer.customerName}
                        </h6>
                        <small className="text-muted">
                          {customer.customerId}
                        </small>
                      </div>
                      <p className="mb-1 text-muted small">
                        {customer.address}
                      </p>
                    </button>
                  ))}
                </div>
              )}

              {/* 4. 데이터가 없거나, 검색 결과가 없을 때 */}
              {!loading && customerPageData.content.length === 0 && (
                <div className="text-center text-muted py-5">
                  {customerSearchTerm
                    ? "검색 결과가 없습니다."
                    : "등록된 거래처가 없습니다."}
                </div>
              )}
            </div>

            <div className="modal-footer d-flex justify-content-between">
              {/* 5. 모달 내 페이지네이션 (거래처 전용) */}
              {customerPageData && customerPageData.totalPages > 1 && (
                <Pagination className="mb-0">
                  <Pagination.First
                    onClick={() => setCustomerCurrentPage(0)}
                    disabled={customerPageData.first}
                  />
                  <Pagination.Prev
                    onClick={() =>
                      setCustomerCurrentPage(customerCurrentPage - 1)
                    }
                    disabled={customerPageData.first}
                  />
                  {createPaginationItems(
                    customerPageData,
                    setCustomerCurrentPage
                  )}
                  <Pagination.Next
                    onClick={() =>
                      setCustomerCurrentPage(customerCurrentPage + 1)
                    }
                    disabled={customerPageData.last}
                  />
                  <Pagination.Last
                    onClick={() =>
                      setCustomerCurrentPage(customerPageData.totalPages - 1)
                    }
                    disabled={customerPageData.last}
                  />
                </Pagination>
              )}
              <button
                type="button"
                className="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* --- [담당자 선택 모달] (검색 버튼 적용) --- */}
      <div
        className="modal fade"
        id="managerListModal"
        tabIndex="-1"
        aria-labelledby="managerModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-scrollable">
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5" id="managerModalLabel">
                담당자 선택
              </h1>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>

            <div className="modal-body">
              {/* [수정] 1. 검색창 + 검색 버튼 (Input Group) */}
              <div className="input-group mb-3">
                <input
                  type="text"
                  className="form-control"
                  placeholder="담당자 코드 또는 이름으로 검색..."
                  value={managerInputValue} // ⬅️ 타이핑용 state
                  onChange={handleManagerInputChange} // ⬅️ 타이핑 핸들러
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      e.preventDefault();
                      handleManagerSearchSubmit();
                    }
                  }} // ⬅️ Enter 키 핸들러
                />
                <button
                  className="btn btn-primary"
                  type="button"
                  onClick={handleManagerSearchSubmit} // ⬅️ 검색 버튼 핸들러
                >
                  검색
                </button>
              </div>

              {/* 2. 로딩 상태 표시 */}
              {loading && (
                <div className="d-flex justify-content-center my-5">
                  <div className="spinner-border" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </div>
                </div>
              )}

              {/* 3. 데이터가 있을 때 List Group으로 표시 */}
              {!loading && managerPageData.content.length > 0 && (
                <div className="list-group manager-list">
                  {managerPageData.content.map((manager) => (
                    <button
                      type="button"
                      key={manager.id}
                      className="list-group-item list-group-item-action"
                      data-bs-dismiss="modal"
                      onClick={() => handleManagerSelect(manager)}
                    >
                      <div className="d-flex w-100 justify-content-between">
                        <h6 className="mb-1 fw-bold">{manager.name}</h6>
                        <small className="text-muted">{manager.username}</small>
                      </div>
                      <p className="mb-1 text-muted small">{manager.email}</p>
                    </button>
                  ))}
                </div>
              )}

              {/* 4. 데이터가 없거나, 검색 결과가 없을 때 */}
              {!loading && managerPageData.content.length === 0 && (
                <div className="text-center text-muted py-5">
                  {managerSearchTerm
                    ? "검색 결과가 없습니다."
                    : "등록된 담당자가 없습니다."}
                </div>
              )}
            </div>

            <div className="modal-footer d-flex justify-content-between">
              {/* 5. 모달 내 페이지네이션 (담당자 전용) */}
              {managerPageData && managerPageData.totalPages > 1 && (
                <Pagination className="mb-0">
                  <Pagination.First
                    onClick={() => setManagerCurrentPage(0)}
                    disabled={managerPageData.first}
                  />
                  <Pagination.Prev
                    onClick={() =>
                      setManagerCurrentPage(managerCurrentPage - 1)
                    }
                    disabled={managerPageData.first}
                  />
                  {createPaginationItems(
                    managerPageData,
                    setManagerCurrentPage
                  )}
                  <Pagination.Next
                    onClick={() =>
                      setManagerCurrentPage(managerCurrentPage + 1)
                    }
                    disabled={managerPageData.last}
                  />
                  <Pagination.Last
                    onClick={() =>
                      setManagerCurrentPage(managerPageData.totalPages - 1)
                    }
                    disabled={managerPageData.last}
                  />
                </Pagination>
              )}
              <button
                type="button"
                className="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default OrderHeader;
