import React, { useState, useMemo, useEffect, useCallback } from "react"; // 1. useCallback 제거
import { Link } from "react-router-dom";
import axios from "axios";
import qs from "qs";
import { Pagination } from "react-bootstrap";
import {
  IoHourglassOutline,
  IoClipboardOutline,
  IoBuildOutline,
  IoCubeOutline,
  IoCheckmarkDoneCircleOutline,
  IoBagCheckOutline,
  IoCarOutline, // (IoCarOutline은 현재 사용되지 않음)
  IoStorefrontOutline,
  IoArrowForwardCircleOutline,
} from "react-icons/io5";

// ... (ASSEMBLY_STATUS, getStatusProps, ORDER_STATUS, getOrderStatusProps 정의는 동일) ...
// (AssemblyStatusTag, OrderStatusTag 컴포넌트 정의는 동일) ...

// [복사-붙여넣기 하실 때, 위쪽의 Helper 함수/객체들도 포함해야 합니다]

// 5단계 조립 상태(Assembly Status) 정의
const ASSEMBLY_STATUS = {
  QUEUE: "QUEUE",
  INSPECTING: "INSPECTING",
  ASSEMBLY_COMPLETE: "ASSEMBLY_COMPLETE",
  SHIPPING_WAIT: "SHIPPING_WAIT",
  SHIPPING_PENDING: "SHIPPING_PENDING",
  COMPLETED: "COMPLETED",
};

// 상태에 따른 아이콘, 색상, 레이블을 반환하는 헬퍼 함수
const getStatusProps = (status) => {
  switch (status) {
    case ASSEMBLY_STATUS.QUEUE:
      return {
        Icon: IoHourglassOutline,
        label: "작업 대기",
        colorClass: "text-secondary bg-light border",
      };
    case ASSEMBLY_STATUS.INSPECTING:
      return {
        Icon: IoClipboardOutline,
        label: "부품 검수 중",
        colorClass: "text-primary bg-primary-subtle border-primary-subtle",
      };
    case ASSEMBLY_STATUS.ASSEMBLY_COMPLETE:
      return {
        Icon: IoBuildOutline,
        label: "조립 및 BIOS 완료",
        colorClass: "text-warning bg-warning-subtle border-warning-subtle",
      };
    case ASSEMBLY_STATUS.SHIPPING_WAIT:
      return {
        Icon: IoCubeOutline,
        label: "운송장 등록 대기",
        colorClass: "text-info bg-info-subtle border-info-subtle",
      };

    case ASSEMBLY_STATUS.COMPLETED:
      return {
        Icon: IoCheckmarkDoneCircleOutline,
        label: "출고 완료",
        colorClass: "text-success bg-success-subtle border-success-subtle",
      };
    default:
      return {
        Icon: IoHourglassOutline,
        label: "상태 미확인",
        colorClass: "text-danger bg-danger-subtle border-danger-subtle",
      };
  }
};

const ORDER_STATUS = {
  PAID: "PAID", // 결제완료
  PREPARING_PRODUCT: "PREPARING_PRODUCT",
  SHIPPING_PENDING: "SHIPPING_PENDING",
  SHIPPING: "SHIPPING", // 배송중
  DELIVERED: "DELIVERED", // 배송완료
};

const ORDER_STATUS_FLOW = {
  [ORDER_STATUS.PAID]: { nextLabel: "조립중 " },
  [ORDER_STATUS.PREPARING_PRODUCT]: { nextLabel: "배송 대기" },
  [ORDER_STATUS.SHIPPING_PENDING]: { nextLabel: "배송 중" },
  [ORDER_STATUS.SHIPPING]: { nextLabel: "배송 완료" },
  [ORDER_STATUS.DELIVERED]: { nextLabel: null }, // 마지막 단계
};

// 주문 상태에 따른 아이콘, 색상, 레이블을 반환하는 헬퍼 함수
const getOrderStatusProps = (status) => {
  let baseProps;
  switch (status) {
    case ORDER_STATUS.PAID:
      baseProps = {
        Icon: IoBagCheckOutline,
        label: "결제 완료",
        colorClass: "text-success bg-success-subtle border-success-subtle",
      };
      break;
    case ORDER_STATUS.PREPARING_PRODUCT:
      baseProps = {
        Icon: IoStorefrontOutline,
        label: " 조립 중",
        colorClass: "text-info bg-info-subtle border-info-subtle",
      };
      break;
    case ORDER_STATUS.SHIPPING_PENDING:
      baseProps = {
        Icon: IoCubeOutline,
        label: "배송 대기",
        colorClass: "text-primary bg-primary-subtle border-primary-subtle",
      };
      break;
    case ORDER_STATUS.SHIPPING:
      baseProps = {
        Icon: IoCubeOutline,
        label: "배송 중",
        colorClass: "text-primary bg-primary-subtle border-primary-subtle",
      };
      break;
    case ORDER_STATUS.DELIVERED:
      baseProps = {
        Icon: IoCheckmarkDoneCircleOutline,
        label: "배송 완료",
        colorClass: "text-success bg-success-subtle border-success-subtle",
      };
      break;
    default:
      baseProps = {
        Icon: IoHourglassOutline,
        label: "상태 미확인",
        colorClass: "text-danger bg-danger-subtle border-danger-subtle",
      };
  }

  const nextStepLabel = ORDER_STATUS_FLOW[status]?.nextLabel;
  return { ...baseProps, nextStepLabel };
};

// 상태를 시각적으로 표시하는 재사용 가능한 컴포넌트
const AssemblyStatusTag = ({ status }) => {
  const { Icon, label, colorClass } = getStatusProps(status);
  return (
    <span
      className={`badge fs-6 fw-semibold d-inline-flex align-items-center px-3 py-2 rounded-pill ${colorClass}`}
    >
      <Icon className="me-2" size={16} />
      {label}
    </span>
  );
};

const OrderStatusTag = ({ status }) => {
  const { Icon, label, colorClass, nextStepLabel } =
    getOrderStatusProps(status);

  // colorClass에서 배경색 관련 클래스는 제외하고 텍스트 색상 클래스만 사용
  const textColor = colorClass.split(" ").find((c) => c.startsWith("text-"));

  return (
    <div className="d-flex flex-column align-items-center">
      {/* 현재 상태: 아이콘과 텍스트만으로 간결하게 표시, small 태그로 폰트 크기 축소 */}
      <small
        className={`d-inline-flex align-items-center ${
          textColor || "text-muted"
        }`}
      >
        {Icon && <Icon className="me-2" size={14} />}
        <span className="fw-semibold">{label}</span>
      </small>

      {/* 다음 단계 힌트 (있을 경우에만 표시) */}
      {nextStepLabel && (
        <small className="text-muted mt-1 d-inline-flex align-items-center">
          <IoArrowForwardCircleOutline size={12} className="me-1 opacity-75" />
          <span className="fw-light">다음: {nextStepLabel}</span>
        </small>
      )}
    </div>
  );
};

// 작업 대기 리스트 메인 컴포넌트
const AssemblyQueue = () => {
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
  const [tableLoading, setTableLoading] = useState(true); // 📌 [추가] 로딩 상태

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

  // 📌 [수정] API 호출 로직을 useEffect 밖으로 분리합니다.
  //    (useCallback으로 감싸서 불필요한 재생성을 방지합니다.)
  const fetchOrders = useCallback(async (pageTofetch) => {
    setTableLoading(true);

    // API 호출에 사용할 상태 목록
    const statusesToFetch = [
      "PAID",
      "PREPARING_PRODUCT",
      "SHIPPING_PENDING",
      "SHIPPING",
    ];

    try {
      const resp = await axios.get("/api/orders/assembly-queue", {
        params: {
          orderStatus: statusesToFetch,
          page: pageTofetch,
          size: 10,
          sort: "orderId,desc",
        },
        paramsSerializer: (params) => {
          return qs.stringify(params, { arrayFormat: "comma" });
        },
      });
      console.log("resp ", resp);
      setOrders(resp.data.content || []); // content가 없으면 빈 배열
      setPageData(resp.data);
    } catch (error) {
      console.error("작업 대기 목록 조회 실패:", error);
      // (필요시 setMessage로 에러 알림)
    } finally {
      setTableLoading(false);
    }
  }, []); // (의존성 없음, 이 함수 자체는 변하지 않음)

  // 📌 [수정] useEffect가 'currentPage' 또는 'fetchOrders' 함수가 변경될 때 실행
  useEffect(() => {
    // currentPage(0)로 fetchOrders 함수를 호출
    fetchOrders(currentPage);
  }, [currentPage, fetchOrders]); // currentPage가 바뀌면 fetchOrders가 다시 호출됨

  // 📌 [수정] 검색 기능 (useMemo)
  // [주의!] 이 검색은 현재 페이지(10개) 내에서만 작동합니다.
  // 서버 전체에서 검색하려면 API에 'searchTerm'을 파라미터로 보내야 합니다.
  const filteredOrders = useMemo(() => {
    if (!searchTerm.trim()) return orders; // orders는 pageData.content (최대 10개)
    const lowercasedSearchTerm = searchTerm.toLowerCase();
    return orders.filter(
      (order) =>
        // order.orderId가 숫자일 수 있으므로 문자열로 변환
        String(order.orderId).toLowerCase().includes(lowercasedSearchTerm) ||
        (order.customerName &&
          order.customerName.toLowerCase().includes(lowercasedSearchTerm))
    );
  }, [orders, searchTerm]);

  const handleRowClick = (orderId) => {
    console.log(`주문 ${orderId}의 상세 작업 페이지로 이동합니다.`);
    // 예: navigate(`/admin/assembly/${orderId}`);
  };

  return (
    <div className="container my-5">
      <header className="border-bottom pb-3 mb-4">
        <h1 className="display-5 fw-bold text-primary">작업 대기 리스트</h1>
        <p className="text-muted">조립 및 출고 대기 중인 주문 목록입니다.</p>
      </header>

      <div className="card shadow-sm border-0">
        <div className="card-body p-4">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h5 className="card-title fw-bold mb-0">
              {/* 📌 [수정] 전체 개수는 pageData.totalElements에서 가져옵니다. */}
              진행 중인 작업 ({pageData.totalElements || 0}건)
            </h5>
            <input
              type="text"
              className="form-control"
              style={{ maxWidth: "300px" }}
              placeholder="주문ID 또는 고객명 검색 (현재 페이지)..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="table-responsive">
            <table className="table table-hover table-bordered align-middle">
              <thead className="table-light">
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
                    style={{ width: "16%" }}
                  >
                    고객명
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "16%" }}
                  >
                    담당자
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "16%" }}
                  >
                    결제일
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "23%" }}
                  >
                    조립 상태
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "10%" }}
                  >
                    상세보기
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "14%" }}
                  >
                    배송 상태
                  </th>
                </tr>
              </thead>
              <tbody>
                {tableLoading ? (
                  <tr>
                    <td colSpan="7" className="text-center text-muted py-5">
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
                      <td className="text-center">
                        {order.assemblyWorkerId || (
                          <span className="text-muted fst-italic">미배정</span>
                        )}
                      </td>
                      <td className="text-center">{order.paidAt}</td>
                      <td className="text-center">
                        <AssemblyStatusTag status={order.assemblyStatus} />
                      </td>
                      <td className="text-center">
                        <Link
                          to={`/assembly/detail/${order.orderId}`}
                          className="btn btn-sm btn-primary"
                          onClick={(e) => e.stopPropagation()} // 행 클릭 방지
                        >
                          상세보기
                        </Link>
                      </td>
                      <td className="text-center">
                        <OrderStatusTag status={order.orderStatus} />
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7" className="text-center text-muted py-5">
                      <p className="mb-1">결과가 없습니다.</p>
                      <small>
                        현재 진행 중인 조립 및 출고 대기 건이 없습니다.
                      </small>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* 📌 [수정] Pagination UI 위치 (카드 밖, 중앙 정렬) */}
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

export default AssemblyQueue;
