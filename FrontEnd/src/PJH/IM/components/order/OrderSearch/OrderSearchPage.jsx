import React, { useState, useEffect } from "react";
import axios from "axios";
import OrderFilterBar from "./OrderFilterBar";
import OrderGrid from "./OrderGrid";
import OrderDetailPanel from "./OrderDetailPanel";
import { Pagination } from "react-bootstrap"; // 페이지네이션 추가


const OrderSearchPage = () => {
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  

  // 페이징 상태 추가
  const [currentPage, setCurrentPage] = useState(0);
  const [pageData, setPageData] = useState({
    content: [],
    totalPages: 0,
    number: 0,
    first: true,
    last: true,
  });

  // 검색 조건 상태
  const [searchCondition, setSearchCondition] = useState({
    startDate: "",
    endDate: "",
    orderStatus: "", // 배열 (멀티 선택용)
    keyword: "",
    managerId: "",
  });

  // 조회 함수 (페이지 번호를 인자로 받음)
  const fetchOrders = async (pageToFetch = 0) => {
    setLoading(true);
    try {
      // 1. 파라미터 정리
      const params = {
        ...searchCondition,
        orderStatus: searchCondition.orderStatus, // 배열 -> 문자열 변환
        page: pageToFetch,
        size: 15,
        sort: "orderId,desc",
      };


      const response = await axios.get("/api/orders", { params });

      console.log("검색 결과:", response.data); // ✅ 선언 후 사용 OK

      // 3. 상태 업데이트
      setOrders(response.data.content || []);
      setPageData(response.data);
      setCurrentPage(pageToFetch); // 현재 페이지 상태 동기화

    } catch (error) {
      console.error("주문 조회 실패:", error);
      alert("데이터를 불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 초기 로딩
  useEffect(() => {
    fetchOrders(0);
  }, []); 

  // 검색 버튼 클릭 시 (0페이지부터 다시 검색)
  const handleSearch = () => {
    setCurrentPage(0);
    fetchOrders(0);
  };

  // 페이지 변경 핸들러
  const handlePageChange = (pageNumber) => {
    fetchOrders(pageNumber - 1); // API는 0부터 시작하므로 -1
  };

  const handleCancelOrder= async (orderIdToCancel)=>{
    if (window.confirm("정말로 주문을 취소하시겠습니까?")) {
      try {
        // 1. API 경로도 일관되게 맞춤
        const response = await axios.post(
          `/api/orders/${orderIdToCancel}/cancel`
        );

        // 2. 204 대신 200 OK를 확인
        if (response.status === 200) {
          alert("선택된 주문서가 성공적으로 취소되었습니다.");

          // 3. (핵심) response.data로 받은 '최신 주문 객체'로 상태를 업데이트
          const canceledOrder = response.data;

          setOrders(
            orders.map((order) =>
              order.orderId === orderIdToCancel
                ? canceledOrder // 목록에서 해당 주문을 '최신 데이터'로 교체
                : order
            )
          );

          // 4. 현재 선택된 주문도 최신 데이터로 업데이트
          if (selectedOrder?.orderId === orderIdToCancel) {
            setSelectedOrder(canceledOrder);
          }
        } else {
          throw new Error("취소에 실패했습니다.");
        }
      } catch (err) {
        alert("주문서 취소 중 오류가 발생했습니다.");
        console.error(err);
      }
    }
  }

  const handleForceCancelOrder= async (orderIdToCancel)=>{
    if (window.confirm("정말로 결제 연동 제외 취소를 진행하시겠습니까?")) {
      try {
        const response = await axios.post(
          `/api/orders/admin/${orderIdToCancel}/force-cancel`
        );

        if (response.status === 200) {
          alert("선택된 주문서가 성공적으로 취소되었습니다.");
          console.log("response: ", response);

          const canceledOrder = response.data;

          setOrders(
            orders.map((order) =>
              order.orderId === canceledOrder.orderId
                ? canceledOrder
                : order
            )
          );

          if (selectedOrder?.orderId === orderIdToCancel) {
            setSelectedOrder(canceledOrder);
          }
        } else {
          throw new Error("취소에 실패했습니다.");
        }
      } catch (err) {
        alert("주문서 취소 중 오류가 발생했습니다.");
        console.error(err);
      }
    }
  }
  

  // 페이지네이션 아이템 생성 헬퍼
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
          onClick={() => handlePageChange(number + 1)}
        >
          {number + 1}
        </Pagination.Item>
      );
    }
    return pages;
  };

  return (
    <div className="container-fluid p-4">
      <h2 className="mb-4 fw-bold">통합 주문 조회</h2>

      {/* 1. 상단 필터 바 */}
      <div className="card mb-3 shadow-sm">
        <div className="card-body">
          <OrderFilterBar
            condition={searchCondition}
            setCondition={setSearchCondition}
            onSearch={handleSearch}
          />
        </div>
      </div>

      {/* 2. 하단 컨텐츠 (좌: 그리드, 우: 상세) */}
      <div className="row g-3">
        {/* 왼쪽: 주문 목록 그리드 */}
        <div className={selectedOrder ? "col-lg-8" : "col-12"}>
          <OrderGrid
            orders={orders}
            loading={loading}
            selectedId={selectedOrder?.orderId}
            onSelect={setSelectedOrder}
          />

          {/* 페이지네이션 (중앙 정렬) */}
          <div className="d-flex justify-content-center mt-4">
            {pageData && pageData.totalPages > 1 && (
              <Pagination>
                <Pagination.First 
                  onClick={() => handlePageChange(1)} 
                  disabled={pageData.first} 
                />
                <Pagination.Prev 
                  onClick={() => handlePageChange(pageData.number)} 
                  disabled={pageData.first} 
                />
                {createPaginationItems()}
                <Pagination.Next 
                  onClick={() => handlePageChange(pageData.number + 2)} 
                  disabled={pageData.last} 
                />
                <Pagination.Last 
                  onClick={() => handlePageChange(pageData.totalPages)} 
                  disabled={pageData.last} 
                />
              </Pagination>
            )}
          </div>
        </div>

        {/* 오른쪽: 상세 패널 (선택 시 등장) */}
        {selectedOrder && (
          <div className="col-lg-4">
            <OrderDetailPanel
              order={selectedOrder}
              // 필요한 핸들러 전달 (취소, 상태변경 등)
              handleCancelOrder={handleCancelOrder}
              handleForceCancelOrder= {handleForceCancelOrder}
              onStatusUpdate={() => fetchOrders(currentPage)}
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default OrderSearchPage;