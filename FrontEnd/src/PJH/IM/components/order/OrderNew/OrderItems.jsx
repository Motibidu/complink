import React, { useState, useMemo, useEffect, useCallback } from "react";
import axios from "axios";
import { Pagination } from "react-bootstrap"; // Pagination 컴포넌트 Import
import { IoReorderFourOutline } from "react-icons/io5";

const OrderItems = ({
  orderItems,
  handleItemSelect,
  handleItemsChange,
  handleAddItem,
  handleRemoveItem,
}) => {
  const [activeRowIndex, setActiveRowIndex] = useState(null);
  const [loading, setLoading] = useState(false);

  // --- [수정] 모달용 페이징 및 검색 State ---
  const [inputValue, setInputValue] = useState(""); // 1. 타이핑용
  const [searchTerm, setSearchTerm] = useState(""); // 2. API 호출용
  const [currentPage, setCurrentPage] = useState(0);
  const [pageData, setPageData] = useState({
    content: [],
    totalPages: 0,
    number: 0,
    first: true,
    last: true,
  });
  // ---

  // [수정] fetchItems (useCallback 적용)
  const fetchItems = useCallback(async (pageToFetch, currentSearchTerm) => {
    setLoading(true);
    try {
      // [수정] API 호출 시 페이징 및 검색 파라미터 전달
      const response = await axios.get("/api/items", {
        params: {
            page: pageToFetch,
            size: 10, // 모달에서는 10개씩
            sort: "itemId,desc",
            search: currentSearchTerm,
        }
      });
      console.log(response.data);
      setPageData(response.data); // ⬅️ Page 객체 전체를 저장
    } catch (err) {
      console.error("아이템 목록 로드 실패:", err);
    } finally {
      setLoading(false);
    }
  }, []); // 이 함수는 재생성될 필요 없음

  // [수정] useEffect가 'currentPage' 또는 'searchTerm'이 바뀔 때 API 호출
  useEffect(() => {
    fetchItems(currentPage, searchTerm);
  }, [currentPage, searchTerm, fetchItems]);


  const handleModalItemSelect = (selectedItem) => {
    if (activeRowIndex !== null) {
      handleItemSelect(activeRowIndex, selectedItem); // selectedItem 객체 전체를 전달
    }
  };

  // [추가] 검색창 핸들러
  const handleInputChange = (e) => {
    setInputValue(e.target.value);
  };

  // [추가] "검색" 버튼 클릭 또는 Enter 핸들러
  const handleSearchSubmit = () => {
    setSearchTerm(inputValue); // API 호출 트리거
    setCurrentPage(0); // 1페이지로 리셋
  };

  // [추가] 페이지네이션 UI 생성 헬퍼
  const createPaginationItems = (pageDataToUse, setCurrentPageToUse) => {
    let pages = [];
    const maxPagesToShow = 5;
    let startPage = Math.max(0, pageDataToUse.number - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(pageDataToUse.totalPages - 1, startPage + maxPagesToShow - 1);

    if (endPage - startPage + 1 < maxPagesToShow) {
        startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }

    for (let number = startPage; number <= endPage; number++) {
      pages.push(
        <Pagination.Item
          key={number}
          active={number === pageDataToUse.number}
          onClick={() => setCurrentPageToUse(number)}
        >
          {number + 1}
        </Pagination.Item>
      );
    }
    return pages;
  };


  const partCategories = [
    "CPU",
    "MAINBOARD",
    "RAM",
    "SSD",
    "VGA",
    "PSU",
    "CASE",
    "COOLER",
    "FAN"
  ];
  return (
    <div className="order-items">
      <h3 className="order-items__title">상품 목록</h3>
      <table className="order-items__table">
        <thead className="order-items__head">
          <tr className="order-items__row">
            <th className="order-items__cell col-itemList"></th>
            <th className="order-items__cell col-category">부품 종류</th>
            <th className="order-items__cell col-itemName">상품명</th>
            <th className="order-items__cell col-price">단가</th>
            <th className="order-items__cel col-quantity">수량</th>
            <th className="order-items__cell col_price">공급가액</th>
            <th className="order-items__cell col_price">부가세</th>
            <th className="order-items__cell col-delete"></th>
          </tr>
        </thead>
        <tbody className="order-items__body">
          {orderItems.map((orderItem, index) => (
            <tr key={index} className="order-items__row">
              <td>
                <IoReorderFourOutline
                  className="orderHeader__list"
                  size={25}
                  onClick={() => {
                    // 📌 [수정] 모달 열 때 1페이지, 검색어 초기화
                    setCurrentPage(0);
                    setSearchTerm("");
                    setInputValue("");
                    // fetchItems(0, ""); // (useEffect가 자동으로 호출)
                    setActiveRowIndex(index);
                  }}
                  data-bs-toggle="modal"
                  data-bs-target="#itemListModal"
                />
              </td>
              <td className="order-items__cell">
                <select
                  className="order-items__category"
                  value={orderItem.itemCategory}
                  onChange={(e) => handleItemsChange(index, e)}
                >
                  {partCategories.map((partCategory) => (
                    <option key={partCategory} value={partCategory}>
                      {partCategory}
                    </option>
                  ))}
                </select>
              </td>
              <td className="order-items__cell .order-items__cell--item-name">
                <input
                  className="order-items__input-string"
                  type="text"
                  name="itemName"
                  value={orderItem.itemName}
                  onChange={(e) => handleItemsChange(index, e)}
                  placeholder="상품명"
                />
              </td>

              <td className="order-items__cell">
                <input
                  className="order-items__input"
                  name="unitPrice"
                  value={orderItem.unitPrice}
                  onChange={(e) => handleItemsChange(index, e)}
                />
              </td>
              <td className="order-items__cell">
                <input
                  className="order-items__input"
                  name="quantity"
                  value={Number(orderItem.quantity).toLocaleString()}
                  onChange={(e) => handleItemsChange(index, e)}
                />
              </td>

              <td className="order-items__cell cell-number">
                <span className="order-items__total">
                  {Number(orderItem.totalPrice).toLocaleString()}
                </span>
              </td>
              <td className="order-items__cell cell-number">
                <span className="order-items__input" name="vatPrice">
                  {Math.round(orderItem.totalPrice * 0.1).toLocaleString()}
                </span>
              </td>
              <td className="order-items__cell cell-center">
                <button
                  type="button"
                  onClick={() => handleRemoveItem(index)}
                  className="order-items__button order-items__button--remove"
                >
                  삭제
                </button>
              </td>
            </tr>
          ))}
          <tr className="order-items__result">
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td className="cell-number">
              <span className="order-items__total">
                {orderItems
                  .reduce((acc, item) => {
                    return acc + Number(item.quantity);
                  }, 0)
                  .toLocaleString()}
              </span>
            </td>

            <td className="cell-number">
              <span className="order-items__total">
                {orderItems
                  .reduce((acc, item) => {
                    return acc + item.totalPrice;
                  }, 0)
                  .toLocaleString()}
              </span>
            </td>
            <td className="cell-number">
              <span className="order-items__total">
                {Math.round(
                  orderItems.reduce((acc, item) => {
                    return acc + item.totalPrice * 0.1;
                  }, 0)
                ).toLocaleString()}
              </span>
            </td>
            <td className="cell-number">
              <span className="order-items__total">
                {Math.round(
                  orderItems.reduce((acc, item) => acc + item.totalPrice, 0) *
                    1.1
                ).toLocaleString()}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      <button
        type="button"
        onClick={handleAddItem}
        className="order-items__button order-items__button--add"
      >
        상품 추가
      </button>

      {/* --- [품목 선택 모달] (페이징/검색 적용) --- */}
      <div
        className="modal fade"
        id="itemListModal"
        tabIndex="-1"
        aria-labelledby="itemModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-scrollable">
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5" id="itemModalLabel">
                품목 선택
              </h1>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>

            <div className="modal-body">
              {/* 1. 검색창 + 검색 버튼 (Input Group) */}
              <div className="input-group mb-3">
                <input
                  type="text"
                  className="form-control"
                  placeholder="품목 코드 또는 이름으로 검색..."
                  value={inputValue} // ⬅️ 타이핑용
                  onChange={handleInputChange} // ⬅️ 타이핑 핸들러
                  onKeyDown={(e) => {if(e.key === 'Enter'){e.preventDefault(); handleSearchSubmit()}}} // ⬅️ Enter 키 핸들러
                />
                <button 
                  className="btn btn-primary" 
                  type="button" 
                  onClick={handleSearchSubmit} // ⬅️ 검색 버튼 핸들러
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
              {/* 📌 [수정] filteredItems -> pageData.content */}
              {!loading && pageData.content.length > 0 && (
                <div className="list-group manager-list">
                  {pageData.content.map((item) => (
                    <button
                      type="button"
                      key={item.itemId}
                      className="list-group-item list-group-item-action"
                      data-bs-dismiss="modal"
                      onClick={() => handleModalItemSelect(item)} // item 객체 전체를 전달
                    >
                      <div className="d-flex w-100 justify-content-between">
                        <h6 className="mb-1 fw-bold">
                          {item.itemId}. {item.itemName}
                        </h6>
                        <small className="text-muted">{item.itemCategory}</small>
                      </div>
                      <p className="mb-1 text-muted small">
                        입고단가: {item.purchasePrice}
                      </p>
                      <p className="mb-1 text-muted small">
                        출고단가: {item.sellingPrice}
                      </p>
                    </button>
                  ))}
                </div>
              )}

              {/* 4. 데이터가 없거나, 검색 결과가 없을 때 */}
              {/* 📌 [수정] filteredItems -> pageData.content */}
              {!loading && pageData.content.length === 0 && (
                <div className="text-center text-muted py-5">
                  {searchTerm
                    ? "검색 결과가 없습니다."
                    : "등록된 품목이 없습니다."}
                </div>
              )}
            </div>

            <div className="modal-footer d-flex justify-content-between">
              {/* 5. [추가] 모달 내 페이지네이션 */}
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
                    {createPaginationItems(pageData, setCurrentPage)}
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
    </div>
  );
};

export default OrderItems;