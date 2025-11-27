import React, { useState, useCallback, useEffect } from "react";
import axios from "axios"; // axios import 추가
import qs from "qs";
import { Pagination } from "react-bootstrap"; // React-Bootstrap의 Pagination 컴포넌트

const CustomerFormPage = () => {
  // 폼 데이터를 한 번에 관리하기 위한 state
  const [customers, setCustomers] = useState([]); // 현재 페이지의 고객 목록

  // 페이징 관련 상태
  const [currentPage, setCurrentPage] = useState(0); // 0-indexed (Spring Pageable의 기본값)
  const [pageData, setPageData] = useState({
    content: [], // 현재 페이지의 데이터 목록
    totalPages: 0, // 전체 페이지 수
    number: 0, // 현재 페이지 번호 (0부터 시작)
    first: true, // 첫 페이지인지
    last: true, // 마지막 페이지인지
  });

  const [selectedCustomers, setSelectedCustomers] = useState([]);
  const [newFormData, setNewFormData] = useState({
    customerId: "CUST-1000",
    customerName: "리보시스템즈",
    phoneNumber: "010-1111-2222",
    email: "kmwon@example.com",
    address: "서울시 강남구 테헤란로 1",
  });

  const [editFormData, setEditFormData] = useState({
    customerId: "",
    customerName: "",
    phoneNumber: "",
    email: "",
    address: "",
  });

  // API 요청 상태(로딩)를 관리하는 state
  const [loading, setLoading] = useState(false); // (삭제/수정/등록 시 로딩 상태)
  const [tableLoading, setTableLoading] = useState(true); // 테이블 데이터 로딩
  // 사용자에게 보여줄 메시지(성공/실패)를 관리하는 state
  const [message, setMessage] = useState({ type: "", text: "" });

  // API로부터 데이터를 불러오는 함수
  const fetchCustomers = async (pageToFetch) => {
    setTableLoading(true);
    try {
      // API 호출 시 page, size, sort 파라미터를 params로 전달
      const response = await axios.get("/api/customers", {
        params: {
          page: pageToFetch,
          size: 15, // 한 페이지에 10개씩
          sort: "customerId,desc", // 최신순 정렬 (백엔드 엔티티 필드명 기준)
        },
      });

      // Spring Boot가 보낸 Page 객체를 상태에 저장
      const customersData = response.data.content || [];
      setCustomers(customersData); // 테이블 렌더링을 위해 customers 상태 업데이트
      setPageData(response.data); // 페이지네이션 UI를 위해 pageData 상태 업데이트
    } catch (error) {
      console.error("거래처 목록을 불러오는 데 실패했습니다.", error);
      setMessage({
        type: "danger",
        text: "데이터를 불러오는 데 실패했습니다.",
      });
    } finally {
      setTableLoading(false);
    }
  };

  // useEffect가 currentPage(페이지 번호)가 바뀔 때마다 실행되도록 변경
  useEffect(() => {
    fetchCustomers(currentPage);
  }, [currentPage]); // currentPage가 변경되면 목록을 다시 불러옵니다.

  //handle select
  const handleSelectAll = (e) => {
    if (e.target.checked) {
      const allCustomerIds = customers.map((customer) => customer.customerId);
      setSelectedCustomers(allCustomerIds);
    } else {
      setSelectedCustomers([]);
    }
  };

  const handleSelectCustomer = (customerId) => {
    if (selectedCustomers.includes(customerId)) {
      setSelectedCustomers((prevSelected) =>
        prevSelected.filter((id) => id !== customerId)
      );
    } else {
      setSelectedCustomers((prevSelected) => [...prevSelected, customerId]);
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedCustomers.length === 0) {
      alert("삭제할 거래처를 선택해주세요.");
      return;
    }

    if (
      window.confirm(
        `선택된 ${selectedCustomers.length}개의 거래처를 정말 삭제하시겠습니까?`
      )
    ) {
      try {
        await axios.delete("/api/customers", {
          params: {
            ids: selectedCustomers,
          },
          paramsSerializer: (params) => {
            return qs.stringify(params, { arrayFormat: "comma" });
          },
        });

        alert("선택된 거래처가 삭제되었습니다.");

        // 목록 새로고침 (현재 페이지 유지 또는 이전 페이지로 이동)
        if (
          pageData.content.length === selectedCustomers.length &&
          currentPage > 0
        ) {
          setCurrentPage(currentPage - 1); // useEffect가 알아서 fetchCustomers 호출
        } else {
          fetchCustomers(currentPage); // 현재 페이지만 새로고침
        }
        setSelectedCustomers([]); // 선택 상태 초기화
      } catch (error) {
        console.error("거래처 삭제 중 오류 발생:", error);
        alert("삭제 중 오류가 발생했습니다.");
      }
    }
  };

  // handle changes
  const handleNewFormChange = (e) => {
    const { name, value } = e.target;
    setNewFormData((prevState) => ({
      ...prevState,
      [name]: value,
    }));
  };

  const handleEditFormChange = (e) => {
    const { name, value } = e.target;
    setEditFormData((prevState) => ({ ...prevState, [name]: value }));
  };

  const handleNewFormSubmit = async (e) => {
    e.preventDefault(); // 폼의 기본 새로고침 동작 방지

    if (!newFormData.customerName) {
      setMessage({
        type: "danger",
        text: "거래처명은 필수 항목입니다.",
      });
      return;
    }

    setLoading(true);
    setMessage({ type: "", text: "" });

    try {
      console.log(newFormData);
      const response = await axios.post("/api/customers", newFormData);

      if (response.status === 201 || response.status === 200) {
        setNewFormData({
          customerId: "",
          customerName: "",
          phoneNumber: "",
          email: "",
          address: "",
        });

        // 새 항목은 1페이지에 있으므로 0페이지로 이동
        setCurrentPage(0);

        alert("거래처 등록이 성공적으로 완료되었습니다.");
        const modalElement = document.getElementById("newFormModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message || "거래처 등록 중 오류가 발생했습니다.";
      setMessage({ type: "danger", text: errorMsg });
      console.error("Error submitting form:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditFormSubmit = async (e) => {
    e.preventDefault();

    if (
      !editFormData.customerName ||
      !editFormData.phoneNumber ||
      !editFormData.email ||
      !editFormData.address
    ) {
      setMessage({ type: "danger", text: "필수 항목(*)을 모두 입력해주세요." });
      return;
    }

    setLoading(true);
    setMessage({ type: "", text: "" });

    try {
      const response = await axios.put(
        "/api/customers/" + editFormData.customerId,
        editFormData
      );

      if (response.status === 201 || response.status === 200) {
        setEditFormData({
          customerId: "",
          customerName: "",
          phoneNumber: "",
          email: "",
          address: "",
        });

        // 목록 새로고침 (현재 페이지 유지)
        fetchCustomers(currentPage);

        const modalElement = document.getElementById("editFormModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
        alert("선택한 거래처의 수정이 완료되었습니다.");
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message || "거래처 수정 중 오류가 발생했습니다.";
      setMessage({ type: "danger", text: errorMsg });
      console.error("Error submitting form:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditClick = (customerToEdit) => {
    setEditFormData({
      customerId: customerToEdit.customerId || "",
      customerName: customerToEdit.customerName || "",
      phoneNumber: customerToEdit.phoneNumber || "",
      email: customerToEdit.email || "",
      address: customerToEdit.address || "",
    });
  };

  // 페이지네이션 UI를 위한 페이지 변경 핸들러
  const handlePageChange = (pageNumber) => {
    // Pagination 컴포넌트는 1부터 시작, API는 0부터 시작하므로 -1
    setCurrentPage(pageNumber - 1);
  };

  // 페이지네이션 아이템을 동적으로 생성하는 헬퍼 함수
  const createPaginationItems = () => {
    let pages = [];
    const maxPagesToShow = 5; // 한 번에 보여줄 최대 페이지 버튼 수
    let startPage = Math.max(
      0,
      pageData.number - Math.floor(maxPagesToShow / 2)
    );
    let endPage = Math.min(
      pageData.totalPages - 1,
      startPage + maxPagesToShow - 1
    );

    // 페이지 수가 maxPagesToShow보다 적을 때, startPage가 0이 되도록 조정
    if (endPage - startPage + 1 < maxPagesToShow) {
      startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }

    // 페이지 번호 (1부터 시작하도록 +1)
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

  return (
    <>
      <header className="mb-3">
        <h3>고객등록 리스트</h3>
      </header>
      <div className="table-responsive table-container-scrollable">
        <table className="table table-hover align-middle">
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  onChange={handleSelectAll}
                  checked={
                    customers.length > 0 &&
                    selectedCustomers.length === customers.length
                  }
                />
              </th>
              <th>거래처 코드</th>
              <th>이름</th>
              <th>전화번호</th>
              <th>이메일</th>
              <th>주소</th>
            </tr>
          </thead>
          <tbody>
            {tableLoading ? (
              <tr>
                <td colSpan="6" className="text-center">
                  <div
                    className="spinner-border spinner-border-sm"
                    role="status"
                  >
                    <span className="visually-hidden">Loading...</span>
                  </div>
                </td>
              </tr>
            ) : customers && customers.length > 0 ? (
              customers.map((customer) => (
                <tr key={customer.customerId}>
                  <td>
                    <input
                      type="checkbox"
                      checked={selectedCustomers.includes(customer.customerId)}
                      onChange={() => handleSelectCustomer(customer.customerId)}
                    />
                  </td>
                  <td>{customer.customerId}</td>
                  <td>
                    <a
                      onClick={() => handleEditClick(customer)}
                      href="#"
                      data-bs-toggle="modal"
                      data-bs-target="#editFormModal"
                    >
                      {customer.customerName}
                    </a>
                  </td>
                  <td>{customer.phoneNumber}</td>
                  <td>{customer.email}</td>
                  <td>{customer.address}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="6" className="text-center">
                  데이터가 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* footer 구조 변경 (버튼 그룹 + 페이지네이션) */}
      <footer className="mt-3 d-flex align-items-center position-relative">
        {/* 1. 왼쪽: 버튼 그룹 (absolute로 왼쪽 고정) */}
        <div className="position-absolute start-0">
          <button
            className="btn btn-primary mx-3"
            data-bs-toggle="modal"
            data-bs-target="#newFormModal"
          >
            신규 고객 등록
          </button>
          <button
            className="btn btn-danger me-3"
            onClick={handleDeleteSelected}
          >
            선택 삭제
          </button>
        </div>

        {/* 2. 중앙: 페이지네이션 (w-100으로 너비 채우고 justify-content-center) */}
        <div className="w-100 d-flex justify-content-center">
          {pageData && pageData.totalPages > 1 && (
            <Pagination className="mb-0">
              <Pagination.First
                onClick={() => setCurrentPage(0)}
                disabled={pageData.first}
              />
              <Pagination.Prev
                onClick={() => setCurrentPage(currentPage - 1)}
                disabled={pageData.first}
              />
              {createPaginationItems()}
              <Pagination.Next
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={pageData.last}
              />
              <Pagination.Last
                onClick={() => setCurrentPage(pageData.totalPages - 1)}
                disabled={pageData.last}
              />
            </Pagination>
          )}
        </div>
      </footer>

      {/* 신규 등록 모달 */}
      <div
        className="modal fade"
        id="newFormModal"
        tabIndex="-1"
        aria-labelledby="newFormModal"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleNewFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="newFormModal">
                  신규 거래처 등록
                </h1>
                <button
                  type="button"
                  className="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                <div className="col-md-6">
                  <label htmlFor="customerName" className="form-label">
                    거래처명 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="customerName"
                    name="customerName"
                    value={newFormData.customerName}
                    onChange={handleNewFormChange}
                    required
                  />
                </div>

                {/* 연락처 */}
                <div className="col-md-6">
                  <label htmlFor="phoneNumber" className="form-label">
                    연락처
                  </label>
                  <input
                    type="tel"
                    className="form-control"
                    id="phoneNumber"
                    name="phoneNumber"
                    value={newFormData.phoneNumber}
                    onChange={handleNewFormChange}
                    placeholder="010-1234-5678"
                  />
                </div>

                {/* 이메일 */}
                <div className="col-12">
                  <label htmlFor="email" className="form-label">
                    이메일
                  </label>
                  <input
                    type="email"
                    className="form-control"
                    id="email"
                    name="email"
                    value={newFormData.email}
                    onChange={handleNewFormChange}
                    placeholder="example@company.com"
                  />
                </div>

                {/* 주소 */}
                <div className="col-12">
                  <label htmlFor="address" className="form-label">
                    주소
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="address"
                    name="address"
                    value={newFormData.address}
                    onChange={handleNewFormChange}
                  />
                </div>
              </div>
              {/* 메시지 표시 영역 */}
              {message.text && (
                <div
                  className={`alert alert-${message.type} mt-4`}
                  role="alert"
                >
                  {message.text}
                </div>
              )}

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  data-bs-dismiss="modal"
                >
                  닫기
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={loading}
                >
                  {loading ? (
                    <span
                      className="spinner-border spinner-border-sm"
                      role="status"
                      aria-hidden="true"
                    ></span>
                  ) : (
                    "저장하기"
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* 수정 모달 */}
      <div
        className="modal fade"
        id="editFormModal"
        tabIndex="-1"
        aria-labelledby="editFormModal"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleEditFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="editFormModal">
                  거래처 정보 수정
                </h1>
                <button
                  type="button"
                  className="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                {/* customerId는 수정하지 않으므로 숨기거나 표시하지 않을 수 있습니다. 
                    다만, PUT 요청 시 필요하므로 editFormData에는 있어야 합니다. */}

                <div className="col-md-6">
                  <label htmlFor="customerName" className="form-label">
                    거래처명 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="customerName"
                    name="customerName"
                    value={editFormData.customerName}
                    onChange={handleEditFormChange}
                    required
                  />
                </div>

                {/* 연락처 */}
                <div className="col-md-6">
                  <label htmlFor="phoneNumber" className="form-label">
                    연락처
                  </label>
                  <input
                    type="tel"
                    className="form-control"
                    id="phoneNumber"
                    name="phoneNumber"
                    value={editFormData.phoneNumber}
                    onChange={handleEditFormChange}
                    placeholder="010-1234-5678"
                  />
                </div>

                {/* 이메일 */}
                <div className="col-12">
                  <label htmlFor="email" className="form-label">
                    이메일
                  </label>
                  <input
                    type="email"
                    className="form-control"
                    id="email"
                    name="email"
                    value={editFormData.email}
                    onChange={handleEditFormChange}
                    placeholder="example@company.com"
                  />
                </div>

                {/* 주소 */}
                <div className="col-12">
                  <label htmlFor="address" className="form-label">
                    주소
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="address"
                    name="address"
                    value={editFormData.address}
                    onChange={handleEditFormChange}
                  />
                </div>
              </div>
              {/* 메시지 표시 영역 */}
              {message.text && (
                <div
                  className={`alert alert-${message.type} mt-4`}
                  role="alert"
                >
                  {message.text}
                </div>
              )}

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  data-bs-dismiss="modal"
                >
                  닫기
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={loading}
                >
                  {loading ? (
                    <span
                      className="spinner-border spinner-border-sm"
                      role="status"
                      aria-hidden="true"
                    ></span>
                  ) : (
                    "저장하기"
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default CustomerFormPage;
