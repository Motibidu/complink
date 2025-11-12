import React, { useState, useEffect } from "react";
import axios from "axios";
import { Pagination } from "react-bootstrap"; // 1. Pagination 컴포넌트 Import

function InventoryStatus() {
  const [items, setItems] = useState([]); // 현재 페이지의 아이템 목록
  const [totalStockAmount, setTotalStockAmount] = useState(0); // "현재 페이지"의 재고 합계
  const [totalPriceAmout, setTotalPriceAmout] = useState(0); // "현재 페이지"의 금액 합계

  // 2. [추가] 페이징 관련 상태
  const [currentPage, setCurrentPage] = useState(0); // 0-indexed
  const [pageData, setPageData] = useState({
    content: [],
    totalPages: 0,
    number: 0,
    first: true,
    last: true,
  });
  const [tableLoading, setTableLoading] = useState(true);

  // 3. [수정] useEffect가 currentPage를 감시하도록 변경
  useEffect(() => {
    const fetchItems = async () => {
      setTableLoading(true);
      try {
        // 4. [수정] API 호출 시 page, size, sort 파라미터를 params로 전달
        const response = await axios.get("/api/items", {
            params: {
              page: currentPage,
              size: 15, 
              sort: "itemId,desc", 
            },
          });

        const itemsData = response.data.content || [];
        setItems(itemsData);
        setPageData(response.data); // 페이지 정보 저장

        // 5. [수정] response.data -> response.data.content (배열)로 변경
        // [주의] 이제 이 합계는 "전체"가 아닌 "현재 페이지"의 합계입니다.
        const pageTotalStock = itemsData.reduce(
          (acc, item) => acc + (item.quantityOnHand || 0), // (null 방지)
          0
        );
        const pageTotalPrice = itemsData.reduce(
          (acc, item) => acc + (item.quantityOnHand || 0) * (item.purchasePrice || 0),
          0
        );
        
        setTotalStockAmount(pageTotalStock);
        setTotalPriceAmout(pageTotalPrice);

      } catch (error) {
        console.error("품목 목록을 불러오는 데 실패했습니다.", error);
      } finally {
        setTableLoading(false);
      }
    };

    fetchItems();
  }, [currentPage]); // 6. [수정] currentPage가 변경될 때마다 목록을 다시 불러옴

  // 7. [추가] 페이지네이션 UI를 위한 헬퍼 함수
  const createPaginationItems = () => {
    let pages = [];
    const maxPagesToShow = 5;
    let startPage = Math.max(0, pageData.number - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(pageData.totalPages - 1, startPage + maxPagesToShow - 1);

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
              <th>입고단가</th>
              <th>금액</th>
            </tr>
          </thead>
          <tbody>
            {tableLoading ? (
                <tr>
                    <td colSpan="5" className="text-center">
                        <div className="spinner-border spinner-border-sm" role="status">
                            <span className="visually-hidden">Loading...</span>
                        </div>
                    </td>
                </tr>
            ) : items && items.length > 0 ? (
              // 8. [수정] items.map() (items는 이제 10개만 들어있음)
              items.map((item) => (
                <tr key={item.itemId}>
                  <td>{item.itemId}</td>
                  <td>
                    {item.itemName}({item.itemCategory}) {/* 9. [수정] category -> itemCategory */}
                  </td>
                  <td>{item.quantityOnHand}</td>
                  <td>{item.purchasePrice.toLocaleString()}</td>
                  <td>
                    {(item.quantityOnHand * item.purchasePrice).toLocaleString()}
                  </td>
                </tr>
              ))
            ) : (
                <tr>
                    <td colSpan="5" className="text-center">데이터가 없습니다.</td>
                </tr>
            )}

            {/* 10. [주의] 이 합계는 "현재 페이지"의 합계입니다. */}
            {!tableLoading && items.length > 0 && (
                <tr className="table-group-divider fw-bold">
                    <td>합계</td>
                    <td>(현재 페이지)</td>
                    <td>{totalStockAmount.toLocaleString()}</td>
                    <td></td>
                    <td>{totalPriceAmout.toLocaleString()}</td>
                </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* 11. [추가] 페이지네이션 UI */}
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
    </>
  );
}

export default InventoryStatus;