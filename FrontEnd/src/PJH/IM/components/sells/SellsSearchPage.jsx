import React, { useState, useEffect, useMemo, useCallback } from "react";
import axios from "axios";
import qs from "qs";
import { Pagination } from "react-bootstrap"; // React-Bootstrap의 Pagination 컴포넌트

const SellsSearchPage = () => {
  const [sells, setSells] = useState([]); // 현재 페이지의 판매 목록
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState({ type: "", text: "" });

  // 페이징 관련 상태
  const [currentPage, setCurrentPage] = useState(0); // 0-indexed
  const [pageData, setPageData] = useState({
    content: [],
    totalPages: 0,
    number: 0,
    first: true,
    last: true,
  });

  // 필터링을 위한 state
  const [searchTerm, setSearchTerm] = useState("");
  const [dateRange, setDateRange] = useState({
    start: "",
    end: new Date().toISOString().slice(0, 10), // 오늘 날짜를 기본값으로 설정
  });
  // const [TrackingNumberReq, setTrackingNumberReq] = useState({
  //   orderId: "",
  //   customerId: "",
  //   trackingNumber: "508368319105",
  //   carrierId: "kr.cjlogistics",
  // });

  // 상세 조회를 위한 state
  const [selectedSell, setSelectedSell] = useState(null); // 'selectedsell' -> 'selectedSell' (카멜케이스)

  // const openTrackingNumberReqModal = (sell) => {
  //   setTrackingNumberReq({
  //     orderId: sell.orderId,
  //     customerId: sell.customerId,
  //     trackingNumber: TrackingNumberReq.trackingNumber,
  //     carrierId: TrackingNumberReq.carrierId,
  //   });
  //   setMessage({ type: "", text: "" }); // 메시지 초기화
  // };

  // const openDeliveryDetailModal = (sell) => {
  //   setSelectedSell(sell);
  // };

  // 📌 [수정] fetchSells가 pageTofetch를 인자로 받고, API 호출 시 params 전달
  const fetchSells = useCallback(async (pageTofetch) => {
    setLoading(true);
    setError(null);
    try {
      const sellsResponse = await axios.get("/api/sells", {
        params: {
          page: pageTofetch,
          size: 15,
          sort: "sellId,desc", // 📌 (주의) 백엔드 Sell 엔티티의 ID 필드명 (sellId) 기준
        },
      });
      console.log("sellsResponse: ", sellsResponse);
      const sellsData = sellsResponse.data.content || [];
      const pageInfo = sellsResponse.data;

      // 💡 각 주문에 대한 배송 추적 등록 상태(Delivery 레코드 존재 여부)를 병렬로 확인
      // (이 로직은 N+1 문제를 유발할 수 있으므로, 백엔드에서 DTO로 묶어오는 것을 권장합니다)
      // (일단 기존 로직 유지)
      // const statusPromises = sellsData.map((sell) =>
      //   fetchDelivery(sell.orderId).then((delivery) => ({
      //     ...sell,
      //     delivery: delivery,
      //   }))
      // );
      // const enrichedSells = await Promise.all(statusPromises);
      // console.log("Enriched Sells Data:", enrichedSells);

      setSells(sellsData); // 📌 enrichedSells -> sellsData (임시)
      setPageData(pageInfo); // 📌 페이지 정보 저장
    } catch (err) {
      setError("판매 데이터를 불러오는 데 실패했습니다.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []); // 📌 useCallback 의존성 배열 비움

  // 📌 [수정] useEffect가 currentPage를 감시
  useEffect(() => {
    fetchSells(currentPage);
  }, [currentPage, fetchSells]); // 📌 fetchSells 추가 (useCallback으로 감싸져 있으므로 안전)

  // 📌 [주의] 이 검색/필터링 로직은 "현재 페이지 10건" 내에서만 작동합니다.
  // 서버 전체에서 검색/필터링하려면 API params에 searchTerm, dateRange를 넘겨야 합니다.
  const filteredsells = useMemo(() => {
    return sells.filter((sell) => {
      // sellDate는 그대로 둡니다.
      const dateTime = new Date(sell.dateTime);
      const startDate = dateRange.start ? new Date(dateRange.start) : null;

      let endDate = null;
      if (dateRange.end) {
        endDate = new Date(dateRange.end);
        endDate.setHours(23, 59, 59, 999);
      }

      if (startDate && dateTime < startDate) return false;
      if (endDate && dateTime > endDate) return false;

      const lowercasedSearchTerm = searchTerm.toLowerCase();

      return (
        sell.customerName?.toLowerCase().includes(lowercasedSearchTerm) ||
        sell.managerName?.toLowerCase().includes(lowercasedSearchTerm)
      );
    });
  }, [sells, searchTerm, dateRange]);

  // 📌 [주의] 이 합계는 "필터링된 현재 페이지"의 합계입니다. (전체 합계 X)
  const totals = useMemo(() => {
    return filteredsells.reduce(
      (acc, sell) => {
        acc.totalAmount += sell.totalAmount;
        acc.vatAmount += sell.vatAmount;
        acc.grandAmount += sell.grandAmount;
        return acc;
      },
      { totalAmount: 0, vatAmount: 0, grandAmount: 0 }
    );
  }, [filteredsells]);

  const handleDateChange = (e) => {
    const { name, value } = e.target;
    setDateRange((prev) => ({ ...prev, [name]: value }));
  };

  const handleViewDetails = (sell) => {
    setSelectedSell(sell);
  };

  // const handleWaybillFormChange = (e) => {
  //   const { name, value } = e.target;
  //   setTrackingNumberReq((prevState) => ({
  //     ...prevState,
  //     [name]: value,
  //   }));
  // };

  // const trackingNumberReqSubmit = async (e) => {
  //   e.preventDefault();
  //   setLoading(true);

  //   try {
  //     console.log("TrackingNumberReq: ", TrackingNumberReq);
  //     const response = await axios.post(
  //       "/api/delivery/trackingNumber",
  //       TrackingNumberReq
  //     );
  //     console.log("response: ", response);

  //     if (response.status === 201 || response.status === 200) {
  //       setTrackingNumberReq({
  //         trackingNumber: "",
  //         carrierId: "",
  //       });
  //       const modalElement = document.getElementById("trackingNumberReqModal");
  //       const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
  //       if (modalInstance) {
  //         modalInstance.hide();
  //       }

  //       // 📌 [수정] 현재 페이지 새로고침
  //       fetchSells(currentPage);

  //       alert(response.data);
  //     }
  //   } catch (error) {
  //     const errorMsg =
  //       error.response?.data?.message ||
  //       "운송장 번호 등록 중 오류가 발생했습니다.";
  //     setMessage({ type: "danger", text: errorMsg });
  //   } finally {
  //     setLoading(false);
  //   }
  // };

  // 📌 [추가] 페이지네이션 UI를 위한 헬퍼 함수
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
    <div className="container py-4">
      <header className="mb-4">
        <h1 className="display-5 fw-bold">판매 조회</h1>
      </header>

      {/* 검색 필터 영역 */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3 align-items-center">
            <div className="col-md-5">
              <input
                type="text"
                className="form-control"
                placeholder="거래처명, 담당자명으로 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <div className="col-md-7">
              <div className="input-group">
                <input
                  type="date"
                  className="form-control"
                  name="start"
                  value={dateRange.start}
                  onChange={handleDateChange}
                />
                <span className="input-group-text">~</span>
                <input
                  type="date"
                  className="form-control"
                  name="end"
                  value={dateRange.end}
                  onChange={handleDateChange}
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 데이터 테이블 */}
      <div className="table-responsive">
        <table className="table table-hover align-middle">
          <thead>
            <tr>
              <th>판매일</th>
              <th>주문번호</th>
              <th>판매번호</th>
              <th>고객명</th>
              <th>담당자명</th>
              <th className="text-end">합계 금액</th>
              {/* <th></th> */}
            </tr>
          </thead>
          <tbody>
            {loading ? (
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
            ) : filteredsells.length > 0 ? (
              filteredsells.map((sell) => {
                // (주석 처리된 배송 로직은 그대로 둡니다)
                return (
                  <tr
                    key={sell.sellId}
                    onClick={() => handleViewDetails(sell)}
                    data-bs-toggle="modal"
                    data-bs-target="#sellDetailModal"
                    style={{ cursor: "pointer" }}
                  >
                    <td>{sell.dateTime.split("T")[0]}</td>
                    <td>{sell.orderId}</td>
                    <td>{sell.sellId}</td>
                    <td>{sell.customerName}</td>
                    <td>{sell.managerName || "-"}</td>
                    <td className="text-end">
                      {sell?.grandAmount?.toLocaleString()}원
                    </td>
                    {/* <td> ... (배송 버튼 로직) ... </td> */}
                  </tr>
                );
              })
            ) : (
              <tr>
                <td colSpan="6" className="text-center text-muted">
                  조회된 데이터가 없습니다.
                </td>
              </tr>
            )}
          </tbody>
          <tfoot>
            <tr className="fw-bold table-group-divider">
              <td colSpan="5" className="text-end">
                조회된 합계 (현재 페이지)
              </td>
              <td className="text-end">
                {totals.grandAmount.toLocaleString()}원
              </td>
            </tr>
          </tfoot>
        </table>
      </div>

      {/* 📌 [추가] 페이지네이션 UI */}
      <footer className="mt-4 d-flex justify-content-center">
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

      {/* 운송장 입력 모달 */}
      {/* <div className="modal fade" id="trackingNumberReqModal" tabIndex="-1">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={trackingNumberReqSubmit}>
              <div className="modal-header">
                <h5 className="modal-title" id="sellDetailModalLabel">
                  운송장 번호/ 택배사 코드 입력
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                <div className="col-md-12 mb-4">
                  <label htmlFor="orderId" className="form-label">
                    주문Id <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="orderId"
                    name="orderId"
                    value={TrackingNumberReq.orderId}
                    readOnly
                    required
                  />
                </div>
                <div className="col-md-12 mb-4">
                  <label htmlFor="wailbillNumber" className="form-label">
                    운송장번호 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="trackingNumber"
                    name="trackingNumber"
                    value={TrackingNumberReq.trackingNumber}
                    onChange={handleWaybillFormChange}
                    required
                  />
                </div>

                <div className="col-md-12">
                  <label htmlFor="carrierId" className="form-label">
                    택배사코드
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="carrierId"
                    name="carrierId"
                    value={TrackingNumberReq.carrierId}
                    onChange={handleWaybillFormChange}
                    required
                  />
                </div>
                {message.text && (
                  <div
                    className={`alert alert-${message.type} mt-4`}
                    role="alert"
                  >
                    {message.text}
                  </div>
                )}
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
                  disabled={loading}
                >
                  {loading ? "저장 중..." : "저장하기"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div> */}

      {/* 판매 상세 정보 모달 */}
      <div className="modal fade" id="sellDetailModal" tabIndex="-1">
        <div className="modal-dialog modal-lg">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title" id="sellDetailModalLabel">
                판매 상세 정보 (판매번호: {selectedSell?.sellId})
              </h5>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>
            <div className="modal-body">
              {selectedSell ? (
                <div>
                  <p>
                    <strong>판매일:</strong>{" "}
                    {selectedSell.dateTime.split("T")[0]}
                  </p>
                  <p>
                    <strong>거래처:</strong> {selectedSell.customerName} (
                    {selectedSell.customerId})
                  </p>
                  <p>
                    <strong>담당자:</strong>{" "}
                    {selectedSell.managerName || "미지정"}
                  </p>
                  <hr />
                  <p>
                    <strong>공급가액:</strong>{" "}
                    {selectedSell.totalAmount.toLocaleString()}원
                  </p>
                  <p>
                    <strong>부가세:</strong>{" "}
                    {selectedSell.vatAmount.toLocaleString()}원
                  </p>
                  <p>
                    <strong>총 합계:</strong>{" "}
                    {selectedSell.grandAmount.toLocaleString()}원
                  </p>
                  <p>
                    <strong>원본 주문번호:</strong> {selectedSell.orderId}
                  </p>
                  <p>
                    <strong>메모:</strong> {selectedSell.memo || "없음"}
                  </p>
                </div>
              ) : (
                <p>상세 정보를 불러올 수 없습니다.</p>
              )}
            </div>
            <div className="modal-footer">
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

      {/* (주석 처리된 deliveryDatailModal은 그대로 둡니다) */}
    </div>
  );
};

export default SellsSearchPage;
