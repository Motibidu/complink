import React, { useState, useEffect } from "react";
import axios from "axios";
import { Pagination, Modal, Button, Form } from "react-bootstrap"; // Modal 관련 컴포넌트 추가
import { useAuth } from "../../contexts/AuthContext"; // 권한 확인용

function InventoryStatus() {
  const { userRole } = useAuth(); // 로그인한 사용자 권한 가져오기 (ADMIN 체크용)

  const [items, setItems] = useState([]); // 현재 페이지의 아이템 목록
  const [totalStockAmount, setTotalStockAmount] = useState(0);
  const [totalPriceAmout, setTotalPriceAmout] = useState(0);

  // 페이징 관련 상태
  const [currentPage, setCurrentPage] = useState(0);
  const [pageData, setPageData] = useState({
    content: [],
    totalPages: 0,
    number: 0,
    first: true,
    last: true,
  });
  const [tableLoading, setTableLoading] = useState(true);

  // --- [추가] 재고 수정 모달 관련 상태 ---
  const [showStockModal, setShowStockModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null); // 수정할 품목 정보
  const [stockFormData, setStockFormData] = useState({
    newQuantity: "",
    reason: "",
  });
  const [modalLoading, setModalLoading] = useState(false); // 모달 저장 중 로딩

  // 목록 조회 함수
  const fetchItems = async () => {
    setTableLoading(true);
    try {
      const response = await axios.get("/api/items", {
        params: {
          page: currentPage,
          size: 15,
          sort: "itemId,desc",
        },
      });
      const itemsData = response.data.content || [];
      setItems(itemsData);
      setPageData(response.data);

      // 현재 페이지 합계 계산
      const pageTotalStock = itemsData.reduce(
        (acc, item) => acc + (item.quantityOnHand || 0),
        0
      );
      const pageTotalPrice = itemsData.reduce(
        (acc, item) =>
          acc + (item.quantityOnHand || 0) * (item.purchasePrice || 0),
        0
      );

      setTotalStockAmount(pageTotalStock);
      setTotalPriceAmout(pageTotalPrice);
    } catch (error) {
      console.error("품목 목록을 불러오는 데 실패했습니다.", error);
      alert("데이터 로딩 실패");
    } finally {
      setTableLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, [currentPage]);

  // --- [추가] 재고 수정 관련 핸들러 ---

  // 수정 버튼 클릭 시 모달 열기
  const handleEditClick = (item) => {
    setSelectedItem(item);
    setStockFormData({
      newQuantity: item.quantityOnHand, // 현재 수량으로 초기화
      reason: "", // 사유 초기화
    });
    setShowStockModal(true);
  };

  // 모달 닫기
  const handleCloseModal = () => {
    setShowStockModal(false);
    setSelectedItem(null);
  };

  // 입력값 변경
  const handleStockFormChange = (e) => {
    const { name, value } = e.target;
    setStockFormData((prev) => ({ ...prev, [name]: value }));
  };

  // 재고 수정 저장 (API 호출)
  const handleStockSubmit = async (e) => {
    e.preventDefault();

    if (!stockFormData.reason) {
      alert("수정 사유를 반드시 입력해야 합니다.");
      return;
    }

    if (window.confirm("정말 재고 수량을 수정하시겠습니까?")) {
      setModalLoading(true);
      try {
        // 백엔드 API 호출 (PUT /api/items/{id}/stock)
        // Body: { quantity: 100, reason: "실사 조정" }
        await axios.put(`/api/items/${selectedItem.itemId}/stock`, {
          quantity: Number(stockFormData.newQuantity),
          reason: stockFormData.reason,
        });

        alert("재고 수정이 완료되었습니다.");
        handleCloseModal();
        fetchItems(); // 목록 새로고침
      } catch (error) {
        console.error("재고 수정 실패:", error);
        const msg =
          error.response?.data?.message || "재고 수정에 실패했습니다.";
        alert(msg);
      } finally {
        setModalLoading(false);
      }
    }
  };

  // 페이지네이션 UI 생성
  const createPaginationItems = () => {
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

  return (
    <>
      <header className="mb-3">
        <h3>재고현황</h3>
      </header>
      <div className="table-responsive table-container-scrollable">
        <table className="table table-hover align-middle">
          <thead>
            <tr>
              <th>품목 코드</th>
              <th>이름 및 카테고리</th>
              <th>총재고수량</th>
              <th>가용재고수량</th>
              <th>입고단가</th>
              <th>금액</th>
              {/* [추가] 관리자일 때만 '관리' 컬럼 표시 */}
              {userRole === "ADMIN" && <th>관리</th>}
            </tr>
          </thead>
          <tbody>
            {tableLoading ? (
              <tr>
                <td colSpan="7" className="text-center">
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
                  <td>{item.itemId}</td>
                  <td>
                    {item.itemName} ({item.itemCategory})
                  </td>
                  <td className="fw-bold text-primary">
                    {item.quantityOnHand.toLocaleString()}
                  </td>
                  <td>{item.availableQuantity.toLocaleString()}</td>
                  <td>{item.purchasePrice.toLocaleString()}</td>
                  <td>
                    {(
                      (item.quantityOnHand || 0) * (item.purchasePrice || 0)
                    ).toLocaleString()}
                  </td>

                  {/* [추가] 관리자일 때만 '수정' 버튼 표시 */}
                  {userRole === "ADMIN" && (
                    <td>
                      <button
                        className="btn btn-sm btn-outline-secondary"
                        onClick={() => handleEditClick(item)}
                      >
                        재고조정
                      </button>
                    </td>
                  )}
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="7" className="text-center">
                  데이터가 없습니다.
                </td>
              </tr>
            )}

            {/* 합계 행 */}
            {!tableLoading && items.length > 0 && (
              <tr className="table-group-divider fw-bold bg-light">
                <td colSpan="2" className="text-center">
                  합계 (현재 페이지)
                </td>
                <td className="text-primary">
                  {totalStockAmount.toLocaleString()}
                </td>
                <td></td>
                <td></td>
                <td>{totalPriceAmout.toLocaleString()}</td>
                {userRole === "ADMIN" && <td></td>}
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <footer className="mt-3 d-flex justify-content-center">
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

      {/* --- [추가] 재고 수정 모달 --- */}
      <Modal
        show={showStockModal}
        onHide={handleCloseModal}
        centered
        backdrop="static"
      >
        <Modal.Header closeButton>
          <Modal.Title>재고 수량 조정</Modal.Title>
        </Modal.Header>
        <form onSubmit={handleStockSubmit}>
          <Modal.Body>
            {selectedItem && (
              <>
                <div className="mb-3">
                  <label className="form-label text-muted">품목명</label>
                  <input
                    type="text"
                    className="form-control"
                    value={selectedItem.itemName}
                    disabled
                    readOnly
                  />
                </div>
                <div className="row">
                  <div className="col-6 mb-3">
                    <label className="form-label text-muted">현재 재고</label>
                    <input
                      type="text"
                      className="form-control"
                      value={selectedItem.quantityOnHand}
                      disabled
                      readOnly
                    />
                  </div>
                  <div className="col-6 mb-3">
                    <label className="form-label fw-bold">변경할 재고</label>
                    <input
                      type="number"
                      className="form-control border-primary"
                      name="newQuantity"
                      value={stockFormData.newQuantity}
                      onChange={handleStockFormChange}
                      required
                      min="0"
                    />
                  </div>
                </div>
                <div className="mb-3">
                  <label className="form-label fw-bold">
                    수정 사유 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    name="reason"
                    placeholder="예: 파손 폐기, 실사 차이 조정"
                    value={stockFormData.reason}
                    onChange={handleStockFormChange}
                    required
                  />
                </div>
                <p className="text-muted small">
                  * 재고 수량을 수정하면 가용 재고도 자동으로 함께 조정됩니다.
                </p>
              </>
            )}
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={handleCloseModal}>
              취소
            </Button>
            <Button variant="primary" type="submit" disabled={modalLoading}>
              {modalLoading ? "저장 중..." : "저장하기"}
            </Button>
          </Modal.Footer>
        </form>
      </Modal>
    </>
  );
}

export default InventoryStatus;
