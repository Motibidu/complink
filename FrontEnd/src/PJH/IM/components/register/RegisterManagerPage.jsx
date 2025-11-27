import React, { useState, useCallback, useEffect } from "react";
import "./RegisterManagerPage.css";
import axios from "axios";
import qs from "qs";
import { Pagination } from "react-bootstrap"; // React-Bootstrap의 Pagination 컴포넌트

const ManagerPage = () => {
  const [managers, setManagers] = useState([]); // 현재 페이지의 담당자 목록

  // 페이징 관련 상태
  const [currentPage, setCurrentPage] = useState(0); // 0-indexed
  const [pageData, setPageData] = useState({
    content: [], // 현재 페이지의 데이터 목록
    totalPages: 0, // 전체 페이지 수
    number: 0, // 현재 페이지 번호 (0부터 시작)
    first: true, // 첫 페이지인지
    last: true, // 마지막 페이지인지
  });

  const [selectedManagers, setSelectedManagers] = useState([]);
  const [newFormData, setNewFormData] = useState({
    managerId: "MGR-0025",
    managerName: "김경원",
    email: "kmwon@example.com",
    phoneNumber: "01011112222",
  });

  const [editFormData, setEditFormData] = useState({
    managerId: "",
    managerName: "",
    email: "",
    phoneNumber: "",
  });

  // 로딩 및 메시지 상태
  const [tableLoading, setTableLoading] = useState(true);
  const [formLoading, setFormLoading] = useState(false);
  const [message, setMessage] = useState({ type: "", text: "" });

  // --- 데이터 처리 함수 ---

  // 백엔드에서 담당자 목록을 불러오는 함수
  const fetchManagers = async (pageToFetch) => {
    setTableLoading(true);
    try {
      // API 호출 시 page, size, sort 파라미터를 params로 전달
      const response = await axios.get("/api/managers", {
        params: {
          page: pageToFetch,
          size: 15, // 한 페이지에 10개씩
          sort: "id,desc", // 최신순 정렬 (백엔드 엔티티 필드명 기준)
        },
      });

      // Spring Boot가 보낸 Page 객체를 상태에 저장
      const managersData = response.data.content || [];
      console.log("response: ", response);
      setManagers(managersData); // 테이블 렌더링을 위해 managers 상태 업데이트
      setPageData(response.data); // 페이지네이션 UI를 위해 pageData 상태 업데이트
    } catch (error) {
      console.error("담당자 목록을 불러오는 데 실패했습니다.", error);
      setMessage({
        type: "danger",
        text: "데이터를 불러오는 데 실패했습니다.",
      });
    } finally {
      setTableLoading(false);
    }
  };

  // 컴포넌트가 처음 마운트될 때 + currentPage가 변경될 때 담당자 목록을 불러옵니다.
  useEffect(() => {
    fetchManagers(currentPage);
  }, [currentPage]);

  // 전체 선택/해제 핸들러
  const handleSelectAll = (e) => {
    if (e.target.checked) {
      // 모든 담당자의 ID를 선택 목록에 추가
      const allManagerIds = managers.map((manager) => manager.managerId);
      setSelectedManagers(allManagerIds);
    } else {
      // 선택 목록을 비움
      setSelectedManagers([]);
    }
  };

  const handleSelectManager = (managerId) => {
    if (selectedManagers.includes(managerId)) {
      // 이미 선택된 항목이면 목록에서 제거
      setSelectedManagers((prevSelected) =>
        prevSelected.filter((id) => id !== managerId)
      );
    } else {
      // 선택되지 않은 항목이면 목록에 추가
      setSelectedManagers((prevSelected) => [...prevSelected, managerId]);
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedManagers.length === 0) {
      alert("삭제할 담당자를 선택해주세요.");
      return;
    }

    if (
      window.confirm(
        `선택된 ${selectedManagers.length}명의 담당자를 정말 삭제하시겠습니까?`
      )
    ) {
      try {
        await axios.delete("/api/managers", {
          params: {
            ids: selectedManagers,
          },
          paramsSerializer: (params) => {
            return qs.stringify(params, { arrayFormat: "comma" });
          },
        });

        alert("선택된 담당자가 삭제되었습니다.");

        // 목록 새로고침 (현재 페이지 유지 또는 이전 페이지로 이동)
        if (
          pageData.content.length === selectedManagers.length &&
          currentPage > 0
        ) {
          setCurrentPage(currentPage - 1); // useEffect가 알아서 fetchManagers 호출
        } else {
          fetchManagers(currentPage); // 현재 페이지만 새로고침
        }
        setSelectedManagers([]); // 선택 상태 초기화
      } catch (error) {
        console.error("담당자 삭제 중 오류 발생:", error);
        alert("삭제 중 오류가 발생했습니다.");
      }
    }
  };

  // 폼 입력값 변경 핸들러
  const handleNewFormChange = (e) => {
    const { name, value } = e.target;
    setNewFormData((prevState) => ({ ...prevState, [name]: value }));
  };

  const handleEditFormChange = (e) => {
    const { name, value } = e.target;
    setEditFormData((prevState) => ({ ...prevState, [name]: value }));
  };

  // 폼 제출(담당자 등록) 핸들러
  const handleNewFormSubmit = async (e) => {
    e.preventDefault();
    if (!newFormData.managerId || !newFormData.managerName) {
      alert("담당자 ID와 이름은 필수입니다.");
      return;
    }

    setFormLoading(true);
    setMessage({ type: "", text: "" });

    try {
      const response = await axios.post("/api/managers", newFormData);
      if (response.status === 201 || response.status === 200) {
        alert("담당자가 성공적으로 등록되었습니다.");
        // 성공 시, 목록을 새로고침하고 폼을 초기화합니다.

        // 새 항목은 1페이지에 있으므로 0페이지로 이동
        setCurrentPage(0);

        setNewFormData({
          managerId: "",
          managerName: "",
          email: "",
          phoneNumber: "",
        });

        // Bootstrap 모달을 수동으로 닫습니다.
        const modalElement = document.getElementById("managerFormModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message || "담당자 등록 중 오류가 발생했습니다.";
      alert(errorMsg);
    } finally {
      setFormLoading(false);
    }
  };

  const handleEditFormSubmit = async (e) => {
    e.preventDefault();
    if (!editFormData.managerId || !editFormData.managerName) {
      alert("담당자 ID와 이름은 필수입니다.");
      return;
    }

    setFormLoading(true);
    setMessage({ type: "", text: "" });

    try {
      const response = await axios.put(
        "/api/managers/" + editFormData.managerId,
        editFormData
      );
      if (response.status === 200) {
        alert("담당자 정보 수정이 성공적으로 등록되었습니다.");

        // 목록 새로고침 (현재 페이지 유지)
        fetchManagers(currentPage);

        setEditFormData({
          managerId: "",
          managerName: "",
          email: "",
          phoneNumber: "",
        });

        // Bootstrap 모달을 수동으로 닫습니다.
        const modalElement = document.getElementById("managerEditModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message ||
        "담당자 정보 수정 중 오류가 발생했습니다.";
      alert(errorMsg);
    } finally {
      setFormLoading(false);
    }
  };

  const handleEditClick = (managerToEdit) => {
    setEditFormData({
      managerId: managerToEdit.managerId || "",
      managerName: managerToEdit.managerName || "",
      email: managerToEdit.email || "",
      phoneNumber: managerToEdit.phoneNumber || "",
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
        <h3>담당자등록 리스트</h3>
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
                    managers.length > 0 &&
                    selectedManagers.length === managers.length
                  }
                />
              </th>
              <th>아이디</th>
              <th>이름</th>
              <th>연락처</th>
              <th>email</th>
              <th>권한</th>
              <th>상태</th>
              <th>등록일</th>
            </tr>
          </thead>
          <tbody>
            {tableLoading ? (
              <tr>
                <td colSpan="5" className="text-center">
                  <div
                    className="spinner-border spinner-border-sm"
                    role="status"
                  >
                    <span className="visually-hidden">Loading...</span>
                  </div>
                </td>
              </tr>
            ) : managers && managers.length > 0 ? (
              managers.map((manager) => (
                <tr key={manager.username}>
                  <td>
                    <input
                      type="checkbox"
                      checked={selectedManagers.includes(manager.managerId)}
                      onChange={() => handleSelectManager(manager.managerId)}
                    />
                  </td>
                  <td>{manager.username}</td>
                  <td>
                    <a
                      onClick={() => handleEditClick(manager)}
                      href="#"
                      data-bs-toggle="modal"
                      data-bs-target="#managerEditModal"
                    >
                      {manager.name}
                    </a>
                  </td>
                  <td>{manager.tel}</td>
                  <td>{manager.email}</td>
                  <td>{manager.role}</td>
                  {manager.active ? (
                    <span className="badge bg-success">재직</span>
                  ) : (
                    <span className="badge bg-secondary">퇴사</span>
                  )}
                  <td>{manager.createdAt}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="text-center">
                  데이터가 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* footer 구조 변경 (버튼 그룹 + 페이지네이션) */}
      <footer className="mt-3 d-flex justify-content-center align-items-center">
        {/* 페이지네이션 UI (React-Bootstrap) */}
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
      </footer>

      {/* 신규 등록 모달 */}
      <div
        className="modal fade"
        id="managerFormModal"
        tabIndex="-1"
        aria-labelledby="managerFormModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleNewFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="managerFormModalLabel">
                  신규 담당자 등록
                </h1>
                <button
                  type="button"
                  className="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label htmlFor="managerId" className="form-label">
                    담당자 ID <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="managerId"
                    name="managerId"
                    value={newFormData.managerId}
                    onChange={handleNewFormChange}
                    required
                    readOnly
                  />
                </div>
                <div className="mb-3">
                  <label htmlFor="managerName" className="form-label">
                    담당자 이름 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="managerName"
                    name="managerName"
                    value={newFormData.managerName}
                    onChange={handleNewFormChange}
                    required
                  />
                </div>
                <div className="mb-3">
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
                  />
                </div>
                <div className="mb-3">
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
                  />
                </div>
              </div>
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
                  disabled={formLoading}
                >
                  {formLoading ? "저장 중..." : "저장하기"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* 수정 모달 */}
      <div
        className="modal fade"
        id="managerEditModal"
        tabIndex="-1"
        aria-labelledby="managerEditModal"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleEditFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="managerEditModal">
                  담당자 정보 수정
                </h1>
                <button
                  type="button"
                  className="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label htmlFor="managerId" className="form-label">
                    담당자 ID <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="managerId"
                    name="managerId"
                    value={editFormData.managerId}
                    onChange={handleEditFormChange}
                    required
                    readOnly
                  />
                </div>
                <div className="mb-3">
                  <label htmlFor="managerName" className="form-label">
                    담당자 이름 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="managerName"
                    name="managerName"
                    value={editFormData.managerName}
                    onChange={handleEditFormChange}
                    required
                  />
                </div>
                <div className="mb-3">
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
                  />
                </div>
                <div className="mb-3">
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
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  data-bs-dismiss="modal"
                  onClick={() =>
                    setEditFormData({
                      managerId: "",
                      managerName: "",
                      email: "",
                      phoneNumber: "",
                    })
                  }
                >
                  닫기
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={formLoading}
                >
                  {formLoading ? "저장 중..." : "저장하기"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default ManagerPage;
