import { useState, useMemo, useEffect } from "react";
import { Link } from "react-router-dom";
import axios from "axios";
import {
  IoHourglassOutline,
  IoClipboardOutline,
  IoBuildOutline,
  IoCubeOutline,
  IoCheckmarkDoneCircleOutline,
  IoBagCheckOutline,
  IoCarOutline,
  IoStorefrontOutline,
  IoArrowForwardCircleOutline,
} from "react-icons/io5";

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
  // 상품준비중
  IN_DELIVERY: "IN_DELIVERY", // 배송중
  DELIVERED: "DELIVERED", // 배송완료
};

const ORDER_STATUS_FLOW = {
  [ORDER_STATUS.PAID]: { nextLabel: "상품 준비중" },
  [ORDER_STATUS.PREPARING_PRODUCT]: { nextLabel: "배송 대기" },
  [ORDER_STATUS.SHIPPING_PENDING]: { nextLabel: "배송 중" },
  [ORDER_STATUS.IN_DELIVERY]: { nextLabel: "배송 완료" },
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
        label: "상품 준비중",
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
        label: "배송중",
        colorClass: "text-primary bg-primary-subtle border-primary-subtle",
      };
      break;
    case ORDER_STATUS.IN_DELIVERY:
      baseProps = {
        Icon: IoCarOutline,
        label: "배송 중",
        colorClass: "text-warning bg-warning-subtle border-warning-subtle",
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
  return (
    <div className="d-flex flex-column align-items-center">
      {/* 현재 상태 태그 */}
      <span
        className={`badge fs-6 fw-semibold d-inline-flex align-items-center px-3 py-2 rounded-pill ${colorClass}`}
        style={{ minWidth: "150px" }} // 너비 고정
      >
        {Icon && <Icon className="me-2" size={16} />}
        {label}
      </span>
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
  // 실제 애플리케이션에서는 이 데이터를 API로부터 받아옵니다. (useEffect + axios)

  useEffect(() => {
    const fetchOrders = async () => {
      const resp = await axios.get(
        "/api/orders/assembly-queue?orderStatus=PAID,PREPARING_PRODUCT, SHIPPING_PENDING, SHIPPING, DELIVERED"
      );
      console.log("resp.data: ", resp.data);
      setOrders(resp.data);
    };
    fetchOrders();
  }, []);

  const [orders, setOrders] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");

  const filteredOrders = useMemo(() => {
    if (!searchTerm.trim()) return orders;
    const lowercasedSearchTerm = searchTerm.toLowerCase();
    return orders.filter(
      (order) =>
        order.orderId.toLowerCase().includes(lowercasedSearchTerm) ||
        order.customerName.toLowerCase().includes(lowercasedSearchTerm)
    );
  }, [orders, searchTerm]);

  const handleRowClick = (orderId) => {
    // 실제 애플리케이션에서는 react-router-dom 등을 사용하여
    // 상세 작업 페이지로 이동하는 로직을 구현합니다.
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
              진행 중인 작업 ({filteredOrders.length}건)
            </h5>
            <input
              type="text"
              className="form-control"
              style={{ maxWidth: "300px" }}
              placeholder="주문ID 또는 고객명 검색..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="table-responsive">
            <table className="table table-hover align-middle">
              <thead className="table-light">
                <tr>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "18%" }}
                  >
                    주문 ID
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "18%" }}
                  >
                    고객명
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    tyle={{ width: "18%" }}
                  >
                    담당자
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    tyle={{ width: "18%" }}
                  >
                    결제일
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "18%" }}
                  >
                    주문 상태
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "18%" }}
                  >
                    조립 상태
                  </th>
                  <th
                    scope="col"
                    className="text-center"
                    style={{ width: "10%" }}
                  ></th>
                </tr>
              </thead>
              <tbody>
                {filteredOrders.length > 0 ? (
                  filteredOrders.map((order) => (
                    <tr
                      key={order.orderId}
                      onClick={() => handleRowClick(order.orderId)}
                      style={{ cursor: "pointer" }}
                    >
                      <td className=" text-center fw-medium">
                        {order.orderId}
                      </td>
                      <td className="text-center">{order.customerName}</td>
                      <td className="text-center">
                        {order.assemblyWorkerId || (
                          <span className="text-muted fst-italic">미배정</span>
                        )}
                      </td>
                      <td className="text-center">{order.paidAt}</td>
                      <td className="text-center">
                        <OrderStatusTag status={order.orderStatus} />
                      </td>
                      <td className="text-center">
                        <AssemblyStatusTag status={order.assemblyStatus} />
                      </td>
                      <td className="text-center">
                        <Link
                          to={`/assembly/detail/${order.orderId}`} // 주문 ID를 URL에 포함하도록 수정 (권장 사항)
                          className="btn btn-sm btn-outline-primary"
                        >
                          상세보기
                        </Link>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="4" className="text-center text-muted py-5">
                      <p className="mb-1">검색 결과가 없습니다.</p>
                      <small>다른 검색어를 입력해 보세요.</small>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AssemblyQueue;
