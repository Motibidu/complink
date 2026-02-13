import React, { useState, useMemo, useEffect, useCallback } from "react";
import { Link } from "react-router-dom"; // (상세보기 링크에 필요)
import axios from "axios";
import qs from "qs";
import { Pagination } from "react-bootstrap";
import {
  IoHourglassOutline,
  IoCubeOutline,
  IoCheckmarkDoneCircleOutline,
  IoBagCheckOutline,
  IoStorefrontOutline,
  IoArrowForwardCircleOutline,
  IoCarOutline,
  IoWarningOutline
} from "react-icons/io5";

// --- (OrderStatus 관련 헬퍼 객체 및 컴포넌트) ---
// AssemblyQueue.jsx와 동일한 헬퍼 함수를 사용합니다.
// (필요시 별도 파일로 분리하여 import 해서 사용하세요)

const DELIVERY_STATUS = {
  UNKNOWN: "UNKNOWN",
  INFORMATION_RECEIVED: "INFORMATION_RECEIVED",
  AT_PICKUP: "AT_PICKUP",
  IN_TRANSIT: "IN_TRANSIT",
  OUT_FOR_DELIVERY: "OUT_FOR_DELIVERY",
  ATTEMPT_FAIL: "ATTEMPT_FAIL",
  DELIVERED: "DELIVERED",
  AVAILABLE_FOR_PICKUP: "AVAILABLE_FOR_PICKUP",
  EXCEPTION: "EXCEPTION",
};

// const ORDER_STATUS_FLOW = {
//   [ORDER_STATUS.PAID]: { nextLabel: "조립중 " },
//   [ORDER_STATUS.PREPARING_PRODUCT]: { nextLabel: "배송 대기" },
//   [ORDER_STATUS.SHIPPING_PENDING]: { nextLabel: "배송 중" },
//   [ORDER_STATUS.SHIPPING]: { nextLabel: "배송 완료" },
//   [ORDER_STATUS.DELIVERED]: { nextLabel: null },
// };

const getDeliveryStatusProps = (status) => {
  // 📌 'status' 변수에는 "IN_TRANSIT"와 같은 '이름(name)'이 넘어옵니다.
  // (이전 ShippingListDto 생성자에서 this.deliveryStatus = deliveryStatus;로 설정했기 때문)
  switch (status) {
    case DELIVERY_STATUS.INFORMATION_RECEIVED:
      return {
        Icon: IoHourglassOutline,
        label: "상품준비중",
        colorClass: "text-secondary bg-light border",
      };
    case DELIVERY_STATUS.AT_PICKUP:
      return {
        Icon: IoBagCheckOutline, // '집화' 아이콘
        label: "집화완료",
        colorClass: "text-info bg-info-subtle border-info-subtle",
      };
    case DELIVERY_STATUS.IN_TRANSIT:
      return {
        Icon: IoCarOutline, // '배송 중' 아이콘
        label: "배송중",
        colorClass: "text-primary bg-primary-subtle border-primary-subtle",
      };
    case DELIVERY_STATUS.OUT_FOR_DELIVERY:
      return {
        Icon: IoStorefrontOutline, // '배달 출발' 아이콘
        label: "배송출발",
        colorClass: "text-primary fw-bold", // ⬅️ 사용자에게 중요하므로 강조
      };
    case DELIVERY_STATUS.DELIVERED:
      return {
        Icon: IoCheckmarkDoneCircleOutline,
        label: "배송완료",
        colorClass: "text-success bg-success-subtle border-success-subtle",
      };
    case DELIVERY_STATUS.ATTEMPT_FAIL:
      return {
        Icon: IoWarningOutline, // '실패' 아이콘
        label: "배달실패",
        colorClass: "text-danger bg-danger-subtle border-danger-subtle",
      };
    case DELIVERY_STATUS.AVAILABLE_FOR_PICKUP:
      return {
        Icon: IoCubeOutline,
        label: "픽업가능", // (경비실, 무인택배함 등)
        colorClass: "text-info bg-info-subtle border-info-subtle",
      };
    case DELIVERY_STATUS.EXCEPTION:
      return {
        Icon: IoWarningOutline, // '예외' 아이콘
        label: "배송예외", // (파손, 분실 등)
        colorClass: "text-danger bg-danger-subtle border-danger-subtle",
      };
    default: // UNKNOWN 포함
      return {
        Icon: IoHourglassOutline,
        label: "상태 미확인",
        colorClass: "text-muted bg-light border",
      };
  }
};

const DeliveryStatusTag = ({ status }) => {
  const { Icon, label, colorClass, nextStepLabel } =
    getDeliveryStatusProps(status);
  const textColor = colorClass.split(" ").find((c) => c.startsWith("text-"));

  return (
    <div className="d-flex flex-column align-items-center">
      <small
        className={`d-inline-flex align-items-center ${
          textColor || "text-muted"
        }`}
      >
        {Icon && <Icon className="me-2" size={14} />}
        <span className="fw-semibold">{label}</span>
      </small>
      {nextStepLabel && (
        <small className="text-muted mt-1 d-inline-flex align-items-center">
          <IoArrowForwardCircleOutline size={12} className="me-1 opacity-75" />
          <span className="fw-light">다음: {nextStepLabel}</span>
        </small>
      )}
    </div>
  );
};
// --- (헬퍼 함수 끝) ---

// 배송 조회 메인 컴포넌트
const ShippingTrackingPage = () => {
  const [orders, setOrders] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [pageData, setPageData] = useState({
    content: [],
    totalPages: 0,
    number: 0,
    first: true,
    last: true,
  });
  const [currentPage, setCurrentPage] = useState(0);
  const [tableLoading, setTableLoading] = useState(true);

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

  // 📌 [수정] 배송 관련 주문만 가져오도록 API 및 파라미터 수정
  const fetchShippingOrders = useCallback(async (pageTofetch) => {
    setTableLoading(true);

    // API 호출에 사용할 상태 목록 (배송중, 배송완료 등)
    // const statusesToFetch = [
    //   "SHIPPING_PENDING", // 배송 대기
    //   "SHIPPING", // 배송 중
    //   "DELIVERED", // 배송 완료
    // ];

    try {
      // 📌 [수정] API 엔드포인트를 배송 조회용으로 변경 (백엔드에 구현 필요)
      const resp = await axios.get("/api/delivery/shipping-list", {
        params: {
          //orderStatus: statusesToFetch,
          page: pageTofetch,
          size: 10,
          sort: "orderId,desc", // (또는 배송 시작일: shippingStartDate,desc)
        },
        paramsSerializer: (params) => {
          return qs.stringify(params, { arrayFormat: "comma" });
        },
      });
      console.log("resp ", resp);
      setOrders(resp.data.content || []);
      setPageData(resp.data);
    } catch (error) {
      console.error("배송 목록 조회 실패:", error);
    } finally {
      setTableLoading(false);
    }
  }, []); // 의존성 없음

  // 📌 [수정] useEffect가 fetchShippingOrders를 호출하도록 변경
  useEffect(() => {
    fetchShippingOrders(currentPage);
  }, [currentPage, fetchShippingOrders]);

  // [주의!] 이 검색은 현재 페이지(10개) 내에서만 작동합니다.
  const filteredOrders = useMemo(() => {
    if (!searchTerm.trim()) return orders;
    const lowercasedSearchTerm = searchTerm.toLowerCase();
    return orders.filter(
      (order) =>
        String(order.orderId).toLowerCase().includes(lowercasedSearchTerm) ||
        (order.customerName &&
          order.customerName.toLowerCase().includes(lowercasedSearchTerm)) ||
        // 📌 [수정] 운송장 번호(trackingNumber)로도 검색 (데이터 구조에 따라 필드명 확인 필요)
        (order.trackingNumber &&
          order.trackingNumber.toLowerCase().includes(lowercasedSearchTerm))
    );
  }, [orders, searchTerm]);

  const handleRowClick = (orderId) => {
    // (배송 상세 모달을 띄우거나, 외부 배송조회 페이지로 이동)
    console.log(`주문 ${orderId}의 배송 상세 정보 보기`);
    // 예: window.open(`https://delivery-tracker.com/track/${order.carrierId}/${order.trackingNumber}`);
  };

  return (
    <div className="container my-5">
      <header className="border-bottom pb-3 mb-4">
        <h1 className="display-5 fw-bold text-primary">배송 조회</h1>
        <p className="text-muted">
          배송 대기, 배송 중, 배송 완료된 주문 목록입니다.
        </p>
      </header>

      <div className="card shadow-sm border-0">
        <div className="card-body p-4">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h5 className="card-title fw-bold mb-0">
              배송 목록 ({pageData.totalElements || 0}건)
            </h5>
            <input
              type="text"
              className="form-control"
              style={{ maxWidth: "300px" }}
              placeholder="주문ID, 고객명, 운송장번호 검색..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="table-responsive">
            <table className="table table-hover table-bordered align-middle">
              <thead className="table-light">
                {/* 📌 [수정] 테이블 헤더 변경 */}
                <tr>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "5%" }}
                  >
                    주문 ID
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "15%" }}
                  >
                    고객명
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "15%" }}
                  >
                    배송 등록일
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "20%" }}
                  >
                    운송장 번호 (택배사)
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "20%" }}
                  >
                    배송 상태
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "10%" }}
                  >
                    상세보기
                  </th>
                </tr>
              </thead>
              <tbody>
                {tableLoading ? (
                  <tr>
                    <td colSpan="6" className="text-center text-muted py-5">
                      <div
                        className="spinner-border spinner-border-sm"
                        role="status"
                      >
                        <span className="visually-hidden">Loading...</span>
                      </div>
                    </td>
                  </tr>
                ) : filteredOrders.length > 0 ? (
                  filteredOrders.map((order) => (
                    <tr
                      key={order.orderId}
                      onClick={() => handleRowClick(order.orderId)}
                      style={{ cursor: "pointer" }}
                    >
                      <td className="text-center fw-medium">{order.orderId}</td>
                      <td className="text-center">{order.customerName}</td>
                      <td className="text-center">{order.createdAt.replace('T', ' ').slice(0, 16)}</td>

                      {/* 📌 [수정] 조립 상태 -> 운송장 번호 */}
                      <td className="text-center">
                        {order.trackingNumber ? (
                          <>
                            <div>{order.trackingNumber}</div>
                            <small className="text-muted">
                              ({order.carrierDisplayName})
                            </small>
                          </>
                        ) : (
                          <span className="text-muted fst-italic">미등록</span>
                        )}
                      </td>

                      <td className="text-center">
                        <DeliveryStatusTag status={order.deliveryStatus} />
                      </td>

                      <td className="text-center">
                        {/* 📌 [수정] 링크 주소를 주문 상세 또는 배송 상세로 변경 */}
                        <Link
                          to={`/order/detail/${order.orderId}`} // (경로는 예시입니다)
                          className="btn btn-sm btn-outline-secondary" // (버튼 스타일 변경)
                          onClick={(e) => e.stopPropagation()}
                        >
                          주문 상세
                        </Link>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" className="text-center text-muted py-5">
                      <p className="mb-1">결과가 없습니다.</p>
                      <small>
                        현재 배송 중이거나 배송 완료된 건이 없습니다.
                      </small>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Pagination UI */}
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
    </div>
  );
};

export default ShippingTrackingPage;
