import React, { useState, useEffect } from "react";
import axios from "axios";
import qs from "qs";
import { Pagination } from "react-bootstrap";
import { useAuth } from "../../contexts/AuthContext";

const RegisterItemPage = () => {
  // 폼 데이터를 한 번에 관리하기 위한 state
  const [items, setItems] = useState([]); // 현재 페이지의 아이템 목록

  const { userRole } = useAuth();

  // 페이징 관련 상태
  const [currentPage, setCurrentPage] = useState(0); // 0-indexed (Spring Pageable의 기본값)
  const [pageData, setPageData] = useState({
    content: [], // 현재 페이지의 데이터 목록
    totalPages: 0, // 전체 페이지 수
    number: 0, // 현재 페이지 번호 (0부터 시작)
    first: true, // 첫 페이지인지
    last: true, // 마지막 페이지인지
  });

  const [selectedItems, setSelectedItems] = useState([]);
  const [message, setMessage] = useState({ type: "", text: "" });
  const [newFormData, setNewFormData] = useState({
    itemName: "인텔 코어i5-14세대 14600K (랩터레이크 리프레시) (정품)",
    itemCategory: "CPU",
    purchasePrice: "264480",
    sellingPrice: "296480",
  });
  const [editFormData, setEditFormData] = useState({
    itemId: "",
    itemName: "",
    itemCategory: "",
    purchasePrice: "",
    sellingPrice: "",
  });
  const [tableLoading, setTableLoading] = useState(true);
  const [formLoading, setFormLoading] = useState(false);
  const [loading, setLoading] = useState(false); // (삭제/수정/등록 시 로딩 상태)

  // API로부터 데이터를 불러오는 함수
  const fetchItems = async (pageToFetch) => {
    setTableLoading(true);
    try {
      // API 호출 시 page, size, sort 파라미터를 params로 전달
      const response = await axios.get("/api/items", {
        params: {
          page: pageToFetch,
          size: 15,
          sort: "itemId,desc", // 최신순 정렬
        },
      });

      // Spring Boot가 보낸 Page 객체를 상태에 저장
      const itemsData = response.data.content || [];
      setItems(itemsData); // 테이블 렌더링을 위해 items 상태 업데이트
      setPageData(response.data); // 페이지네이션 UI를 위해 pageData 상태 업데이트
    } catch (error) {
      console.error("품목 목록을 불러오는 데 실패했습니다.", error);
    } finally {
      setTableLoading(false);
    }
  };

  // useEffect가 currentPage(페이지 번호)가 바뀔 때마다 실행되도록 변경
  useEffect(() => {
    fetchItems(currentPage);
  }, [currentPage]); // currentPage가 변경되면 목록을 다시 불러옵니다.

  //handle select
  const handleSelectAll = (e) => {
    if (e.target.checked) {
      const allItemIds = items.map((item) => item.itemId);
      setSelectedItems(allItemIds);
    } else {
      setSelectedItems([]);
    }
  };

  const handleSelectItem = (itemId) => {
    if (selectedItems.includes(itemId)) {
      setSelectedItems((prevSelected) =>
        prevSelected.filter((id) => id !== itemId)
      );
    } else {
      setSelectedItems((prevSelected) => [...prevSelected, itemId]);
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedItems.length === 0) {
      alert("삭제할 품목을 선택해주세요.");
      return;
    }

    if (
      window.confirm(
        `선택된 ${selectedItems.length}개의 품목을 정말 삭제하시겠습니까?`
      )
    ) {
      try {
        await axios.delete("/api/items", {
          params: {
            ids: selectedItems,
          },
          paramsSerializer: (params) => {
            return qs.stringify(params, { arrayFormat: "comma" });
          },
        });

        alert("선택된 품목들이 삭제되었습니다.");

        // 목록 새로고침 (현재 페이지 유지 또는 이전 페이지로 이동)
        if (
          pageData.content.length === selectedItems.length &&
          currentPage > 0
        ) {
          setCurrentPage(currentPage - 1); // useEffect가 알아서 fetchItems 호출
        } else {
          fetchItems(currentPage); // 현재 페이지만 새로고침
        }
        setSelectedItems([]); // 선택 상태 초기화
      } catch (error) {
        console.error("품목 삭제 중 오류 발생:", error);
        alert("삭제 중 오류가 발생했습니다.");
      }
    }
  };

  // handleChange
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

  // submit
  const handleNewFormSubmit = async (e) => {
    e.preventDefault();

    if (
      !newFormData.itemName ||
      !newFormData.purchasePrice ||
      !newFormData.sellingPrice
    ) {
      setMessage({ type: "danger", text: "필수 항목(*)을 모두 입력해주세요." });
      return;
    }

    setLoading(true); // formLoading -> loading으로 통일
    setMessage({ type: "", text: "" });

    try {
      const response = await axios.post("/api/items", {
        ...newFormData,
        purchasePrice: Number(newFormData.purchasePrice),
        sellingPrice: Number(newFormData.sellingPrice),
      });

      if (response.status === 201) {
        setNewFormData({
          itemName: "",
          itemCategory: "",
          purchasePrice: "",
          sellingPrice: "",
        });

        // 새 항목은 1페이지에 있으므로 0페이지로 이동
        setCurrentPage(0);
        // fetchItems(0); // (setCurrentPage(0)가 useEffect를 트리거하므로 중복 호출 불필요)

        alert("품목 등록이 성공적으로 완료되었습니다.");
        const modalElement = document.getElementById("newFormModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message || "품목 등록 중 오류가 발생했습니다.";
      setMessage({ type: "danger", text: errorMsg });
      console.error("Error submitting form:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditFormSubmit = async (e) => {
    e.preventDefault();

    if (
      !editFormData.itemName ||
      !editFormData.purchasePrice ||
      !editFormData.sellingPrice
    ) {
      setMessage({ type: "danger", text: "필수 항목(*)을 모두 입력해주세요." });
      return;
    }

    setLoading(true);
    setMessage({ type: "", text: "" });

    try {
      const response = await axios.put("/api/items/" + editFormData.itemId, {
        ...editFormData,
        purchasePrice: Number(editFormData.purchasePrice),
        sellingPrice: Number(editFormData.sellingPrice),
      });

      if (response.status === 200) {
        setEditFormData({
          itemId: "",
          itemName: "",
          itemCategory: "",
          purchasePrice: "",
          sellingPrice: "",
        });

        // 목록 새로고침 (현재 페이지 유지)
        fetchItems(currentPage);

        const modalElement = document.getElementById("editFormModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
        alert("선택한 품목의 수정이 완료되었습니다.");
      }
    } catch (error) {
      let errorMsg = "품목 수정 중 오류가 발생했습니다.";
      if (error.response?.data) {
        console.error(error);
        if (typeof error.response.data === "object") {
          errorMsg = Object.values(error.response.data).join(", ");
        } else if (typeof error.response.data === "string") {
          errorMsg = error.response.data;
        }
        setMessage({ type: "danger", text: errorMsg });
      }
    } finally {
      setLoading(false);
    }
  };

  const handleEditClick = (itemToEdit) => {
    setEditFormData({
      itemId: itemToEdit.itemId || "",
      itemName: itemToEdit.itemName || "",
      itemCategory: itemToEdit.itemCategory || "",
      purchasePrice: itemToEdit.purchasePrice || "",
      sellingPrice: itemToEdit.sellingPrice || "",
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
        <h3>품목등록 리스트</h3>
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
                    items.length > 0 && selectedItems.length === items.length
                  }
                />
              </th>
              <th>품목 코드</th>
              <th>이름</th>
              <th>카테고리</th>
              <th>입고가격</th>
              <th>출고가격</th>
            </tr>
          </thead>
          <tbody>
            {/* items.map -> pageData.content.map 또는 items.map (items가 pageData.content로 업데이트되므로) */}
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
            ) : items && items.length > 0 ? (
              items.map((item) => (
                <tr key={item.itemId}>
                  <td>
                    <input
                      type="checkbox"
                      checked={selectedItems.includes(item.itemId)}
                      onChange={() => handleSelectItem(item.itemId)}
                    />
                  </td>
                  <td>{item.itemId}</td>
                  <td>
                    <a
                      onClick={() => handleEditClick(item)}
                      href="#"
                      data-bs-toggle="modal"
                      data-bs-target="#editFormModal"
                    >
                      {item.itemName}
                    </a>
                  </td>
                  <td>{item.itemCategory}</td>
                  <td>{item.purchasePrice}</td>
                  <td>{item.sellingPrice}</td>
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
      <footer className="mt-3 d-flex justify-content-center align-items-center position-relative">
        {/* 1. 왼쪽: 버튼 그룹 (absolute로 왼쪽 고정, 흐름에서 제거됨) */}
        <div className="position-absolute start-0">
          {userRole === "ADMIN" && (
            <>
              <button
                className="btn btn-primary mx-3"
                data-bs-toggle="modal"
                data-bs-target="#newFormModal"
              >
                신규 품목 등록
              </button>
              <button
                className="btn btn-danger me-3"
                onClick={handleDeleteSelected}
              >
                선택 삭제
              </button>
            </>
          )}
        </div>

        {/* 2. 중앙: 페이지네이션 (부모가 justify-content-center 이므로 자동 중앙 정렬) */}
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
        id="newFormModal"
        tabIndex="-1"
        aria-labelledby="itemFormModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleNewFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="itemFormModalLabel">
                  신규 품목 등록
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
                  <label htmlFor="itemName" className="form-label fw-bold">
                    품목명 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="itemName"
                    name="itemName"
                    value={newFormData.itemName}
                    onChange={handleNewFormChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="itemCategory" className="form-label fw-bold">
                    카테고리
                  </label>
                  <select
                    className="form-select"
                    id="itemCategory"
                    name="itemCategory"
                    value={newFormData.itemCategory}
                    onChange={handleNewFormChange}
                  >
                    <option value="">카테고리 선택...</option>
                    <option value="CPU">CPU</option>
                    <option value="메인보드">메인보드</option>
                    <option value="그래픽카드">그래픽카드</option>
                    <option value="RAM">RAM</option>
                    <option value="SSD">SSD</option>
                    <option value="케이스/파워">케이스/파워</option>
                  </select>
                </div>

                <div className="mb-3">
                  <label htmlFor="purchasePrice" className="form-label fw-bold">
                    입고 단가 (원) <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    id="purchasePrice"
                    name="purchasePrice"
                    value={newFormData.purchasePrice}
                    onChange={handleNewFormChange}
                    min="0"
                    required
                    placeholder="숫자만 입력"
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="sellingPrice" className="form-label fw-bold">
                    출고 단가 (원) <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    id="sellingPrice"
                    name="sellingPrice"
                    value={newFormData.sellingPrice}
                    onChange={handleNewFormChange}
                    min="0"
                    required
                    placeholder="숫자만 입력"
                  />
                </div>
              </div>

              {message.text && (
                <div
                  className={`alert alert-${message.type} mt-4 mb-0`}
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
        aria-labelledby="itemFormModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleEditFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="itemFormModalLabel">
                  품목 수정
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
                  <label htmlFor="itemName" className="form-label fw-bold">
                    품목명 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="itemName"
                    name="itemName"
                    value={editFormData.itemName}
                    onChange={handleEditFormChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="itemCategory" className="form-label fw-bold">
                    카테고리
                  </label>
                  <select
                    className="form-select"
                    id="itemCategory"
                    name="itemCategory"
                    value={editFormData.itemCategory}
                    onChange={handleEditFormChange}
                  >
                    <option value="">카테고리 선택...</option>
                    <option value="CPU">CPU</option>
                    <option value="메인보드">메인보드</option>
                    <option value="그래픽카드">그래픽카드</option>
                    <option value="RAM">RAM</option>
                    <option value="SSD">SSD</option>
                    <option value="케이스/파워">케이스/파워</option>
                  </select>
                </div>

                <div className="mb-3">
                  <label htmlFor="purchasePrice" className="form-label fw-bold">
                    입고 단가 (원) <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    id="purchasePrice"
                    name="purchasePrice"
                    value={editFormData.purchasePrice}
                    onChange={handleEditFormChange}
                    min="0"
                    required
                    placeholder="숫자만 입력"
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="sellingPrice" className="form-label fw-bold">
                    출고 단가 (원) <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    id="sellingPrice"
                    name="sellingPrice"
                    value={editFormData.sellingPrice}
                    onChange={handleEditFormChange}
                    min="0"
                    required
                    placeholder="숫자만 입력"
                  />
                </div>
              </div>
              {message.text && (
                <div
                  className={`alert alert-${message.type} mt-4 mb-0`}
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

export default RegisterItemPage;
