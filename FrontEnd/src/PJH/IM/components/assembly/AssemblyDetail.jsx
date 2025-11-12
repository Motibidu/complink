import React, { useState, useMemo, useEffect } from "react";
import {
  IoClipboardOutline,
  IoBuildOutline,
  IoCarOutline,
  IoCheckmarkCircleOutline,
  IoAlertCircleOutline,
  IoReloadCircleOutline,
  IoArrowForward,
} from "react-icons/io5";
import { useParams } from "react-router-dom";
import axios from "axios";

const STATUS_LABELS = {
  INSPECTING: "1. 부품 검수 중 (S/N 입력)",
  ASSEMBLY_COMPLETE: "2. 조립 및 BIOS설치 완료",
  SHIPPING_WAIT: "3. 운송장 등록 대기",
  COMPLETED: "4. 출고 완료",
};

const CARREIRS = [
  { id: "kr.cjlogistics", name: "CJ대한통운" },
  { id: "kr.epost", name: "우체국택배" },
  { id: "kr.lotte", name: "롯데택배" },
  { id: "kr.hanjin", name: "한진택배" },
  { id: "kr.logen", name: "로젠택배" },
];

// --- API 호출 함수 ---
const updateOrderStatus = async (orderId, newStatus, payload = {}) => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/${orderId}/status/${newStatus}`,
      payload
    );
    return response.data;
  } catch (error) {
    console.error(
      `Error updating status for order ${orderId} to ${newStatus}:`,
      error
    );
    throw error;
  }
};
// --- 유틸리티 함수: 상태에 따른 색상/아이콘 매핑 (Bootstrap 클래스로 업데이트) ---
const getStatusProps = (currentStatus) => {
  switch (currentStatus) {
    case "QUEUE":
      return {
        icon: IoClipboardOutline,
        color: "text-primary bg-light border border-primary-subtle", // Primary (파랑 계열)
        text: "작업 대기",
      };
    case "INSPECTING":
      return {
        icon: IoClipboardOutline,
        color: "text-primary bg-light border border-primary-subtle", // Primary (파랑 계열)
        text: "부품 검수 중",
      };
    case "ASSEMBLY_COMPLETE":
      return {
        icon: IoBuildOutline,
        color: "text-warning bg-light border border-warning-subtle", // Warning (노랑 계열)
        text: "조립 및 설치 완료",
      };
    case "SHIPPING_WAIT":
      return {
        icon: IoCarOutline,
        color: "text-info bg-light border border-info-subtle", // Info (하늘색 계열)
        text: "운송장 등록 대기",
      };
    
    case "COMPLETED":
      return {
        icon: IoCheckmarkCircleOutline,
        color: "text-success bg-light border border-success-subtle", // Success (녹색 계열)
        text: "출고 완료",
      };
    default:
      return {
        icon: IoAlertCircleOutline,
        color: "text-secondary bg-light border border-secondary-subtle",
        text: "상태 미확인",
      };
  }
};

// --- 컴포넌트: 단계 표시 바 (Bootstrap 그리드 및 클래스 사용) ---
const WorkflowStep = ({ status }) => {
  const steps = Object.keys(STATUS_LABELS).filter((s) => s !== "QUEUE");
  const currentStatusIndex = steps.indexOf(status);

  return (
    <div className="d-flex justify-content-between align-items-start my-4 p-3 bg-white rounded shadow-sm border">
      {steps.map((stepKey, index) => {
        const isActive = stepKey === status;
        const isCompleted = index < currentStatusIndex;
        const { icon: Icon, text } = getStatusProps(stepKey);

        // Bootstrap 색상 클래스 매핑
        let indicatorColor = isCompleted
          ? "bg-success"
          : isActive
          ? "bg-primary"
          : "bg-secondary-subtle";
        let indicatorShadow = isActive ? "shadow-lg border border-primary" : "";
        let textColor = isActive || isCompleted ? "text-dark" : "text-muted";

        return (
          <React.Fragment key={stepKey}>
            <div className="d-flex flex-column align-items-center flex-grow-1 mx-2">
              <div
                className={`rounded-circle d-flex align-items-center justify-content-center ${indicatorColor} ${indicatorShadow}`}
                style={{ width: "40px", height: "40px", color: "white" }}
              >
                {/* SVG 아이콘 렌더링. color: 'white'를 인라인으로 설정하여 아이콘 색상을 보정 */}
                <Icon width="20" height="20" style={{ color: "white" }} />
              </div>
              <p className={`mt-2 text-center small fw-semibold ${textColor}`}>
                {text}
              </p>
            </div>
            {index < steps.length - 1 && (
              <div
                className={`flex-grow-1 h-1 my-4 mx-3 ${
                  isCompleted ? "bg-success" : "bg-light-subtle"
                }`}
              ></div>
            )}
          </React.Fragment>
        );
      })}
    </div>
  );
};

// --- 메인 애플리케이션 컴포넌트 ---
const AssemblyDetail = () => {
  const [order, setOrder] = useState({
    orderId: "",
    customer: {},
    assemblyStatus: "",
    orderItems: [], // 👈 이 부분이 핵심입니다.
    biosInstalled: false,
  });

  const [trackingInfo, setTrackingInfo] = useState({
    orderId: "",
    customerId: "",
    trackingNumber: "",
    carrierId: "",
  });

  const [isLoading, setIsLoading] = useState(false);
  const { orderId } = useParams();

  useEffect(() => {
    const fetchOrderDetail = async (orderId) => {
      try {
        const resp = await axios.get(
          "/api/orders/" + orderId + "/assembly-detail"
        );
        console.log("resp: ", resp);
        return resp.data;
      } catch (error) {
        console.error(`Error fetching order details for ${orderId}:`, error);
        throw error;
      }
    };
    fetchOrderDetail(orderId).then((data) => {
      setOrder(data);
    });
  }, []);

  // 일련번호가 모두 입력되었는지
  const isSnComplete = useMemo(() => {
    return order.orderItems
      .filter((orderItem) => orderItem.serialNumRequired)
      .every(
        (orderItem) => orderItem.serialNum && orderItem.serialNum.trim() !== ""
      );
  }, [order.orderItems]);

  const handleSnChange = (orderItemId, value) => {
    setOrder((prevOrder) => ({
      ...prevOrder,
      orderItems: prevOrder.orderItems.map((orderItem) =>
        orderItem.orderItemId === orderItemId
          ? { ...orderItem, serialNum: value }
          : orderItem
      ),
    }));
  };

  const handleBiosToggle = () => {
    setOrder((prevOrder) => ({
      ...prevOrder,
      biosInstalled: !prevOrder.biosInstalled,
    }));
  };

  const handleTrackingNumber = (e) => {
    setTrackingInfo((prev) => ({ ...prev, trackingNumber: e.target.value }));
  };

  const handleCarrierId = (carrierId) => {
    setTrackingInfo((prev) => ({ ...prev, carrierId: carrierId }));
  };

  const selectedCarrier = useMemo(() => {
    return (
      CARREIRS.find((c) => c.id === trackingInfo.carrierId) || {
        name: "택배사 선택",
      }
    );
  }, [trackingInfo.carrierId]);

  const showMessage = (msg) => {
    // Bootstrap 환경에서는 토스트나 모달로 대체됩니다.
    console.error(msg);
  };

  // const handleAssemblyStatusChange = async (nextAssemblyStatus) => {
  //   setIsLoading(true);

  //   let updatedOrder = {
  //     nextAssemblyStatus: nextAssemblyStatus,
  //     orderItems: order.orderItems,
  //   };
  //   console.log("updatedOrder: ", updatedOrder);

  //   if (nextAssemblyStatus === "SHIPPING_WAIT") {
  //     if (!order.biosInstalled) {
  //       showMessage(
  //         "운송장 등록 단계로 넘어가려면 'BIOS 설치 완료'를 체크해야 합니다."
  //       );
  //       setIsLoading(false);
  //       return;
  //     }
  //   }

  //   if (nextAssemblyStatus === "COMPLETED") {
  //     if (!trackingInfo.trackingNumber.trim()) {
  //       showMessage("출고를 완료하려면 운송장 번호를 반드시 입력해야 합니다.");
  //       setIsLoading(false);
  //       return;
  //     }
  //   }
  //   try {
  //     const resp = await axios.post(
  //       "/api/orders/" + orderId + "/assembly-status",
  //       updatedOrder
  //     );
  //     setOrder(resp.data);
  //     console.log("resp: ", resp);
  //   } catch (error) {
  //     console.error(`Error updating assembly status for ${orderId}:`, error);
  //   } finally {
  //     setIsLoading(false);
  //   }
  // };
  const handleAssemblyStatusChange = async (nextAssemblyStatus) => {
    setIsLoading(true);

    // --- 1. 단계별 유효성 검사 (Guard Clauses) ---

    if (nextAssemblyStatus === "SHIPPING_WAIT") {
      if (!order.biosInstalled) {
        showMessage(
          "운송장 등록 단계로 넘어가려면 'BIOS 설치 완료'를 체크해야 합니다."
        );
        setIsLoading(false);
        return;
      }
    }

    if (nextAssemblyStatus === "COMPLETED") {
      if (!trackingInfo.trackingNumber.trim()) {
        showMessage("출고를 완료하려면 운송장 번호를 반드시 입력해야 합니다.");
        setIsLoading(false);
        return;
      }
      // 웹훅 등록을 위해 택배사 ID(carrierId)도 필수입니다.
      if (!trackingInfo.carrierId) {
        showMessage("출고를 완료하려면 택배사를 반드시 선택해야 합니다.");
        setIsLoading(false);
        return;
      }
    }

    // --- 2. API 호출 및 상태 업데이트 ---

    try {
      // ⭐️ 1단계: 주문 상태 업데이트 (모든 단계 공통)
      const statusUpdatePayload = {
        nextAssemblyStatus: nextAssemblyStatus,
        orderItems: order.orderItems,
        // ⭐️ COMPLETED일 경우 운송장 정보를 페이로드에 포함!
        ...(nextAssemblyStatus === "COMPLETED" && {
          carrierId: trackingInfo.carrierId,
          trackingNumber: trackingInfo.trackingNumber,
          customerId: order.customer.customerId,
        }),
      };

      // 1-1. DB 상태 변경 및 웹훅 등록을 한 번의 API 호출로 처리
      const resp = await axios.post(
        `/api/orders/${orderId}/assembly-status`,
        statusUpdatePayload
      );

      // 1-2. 로컬 상태를 서버 응답(최신 데이터)으로 업데이트
      setOrder(resp.data);
      showMessage("작업이 성공적으로 처리되었습니다."); // 성공 메시지 추가

      // ⭐️ 2단계: 별도의 웹훅 API 호출 제거!
      // if (nextAssemblyStatus === "COMPLETED") { ... }
    } catch (error) {
      // ... (오류 처리 로직 유지)
    } finally {
      setIsLoading(false);
    }
  };

  // 현재 단계에 따른 메인 액션 버튼 렌더링 (Bootstrap 클래스 사용)
  const renderActionButton = () => {
    const { assemblyStatus, biosInstalled } = order;

    switch (assemblyStatus) {
      case "QUEUE":
        return (
          <button
            onClick={() => handleAssemblyStatusChange("INSPECTING")}
            disabled={isLoading}
            className={`btn btn-primary btn-lg w-100 fw-bold d-flex align-items-center justify-content-center ${
              isLoading ? "opacity-75 disabled" : "shadow-sm"
            }`}
          >
            {isLoading ? (
              <IoReloadCircleOutline
                className="spin me-2"
                width="20"
                height="20"
              />
            ) : (
              <IoArrowForward className="me-2" width="20" height="20" />
            )}
            작업 시작 &rarr; 부품 검수
          </button>
        );

      case "INSPECTING":
        return (
          <button
            onClick={() => handleAssemblyStatusChange("ASSEMBLY_COMPLETE")}
            disabled={!isSnComplete || isLoading}
            className={`btn btn-primary btn-lg w-100 fw-bold d-flex align-items-center justify-content-center ${
              !isSnComplete || isLoading ? "opacity-75 disabled" : "shadow-sm"
            }`}
          >
            {isLoading ? (
              <IoReloadCircleOutline
                className="spin me-2"
                width="20"
                height="20"
              />
            ) : (
              <IoArrowForward className="me-2" width="20" height="20" />
            )}
            필수 S/N 입력 완료 &rarr; 조립 시작
          </button>
        );

      case "ASSEMBLY_COMPLETE":
        return (
          <div className="card p-3 border-warning bg-warning-subtle">
            <p className="card-title fw-semibold text-warning-emphasis d-flex align-items-center mb-3">
              <IoBuildOutline className="me-2" width="18" height="18" />
              조립 및 설치 최종 확인
            </p>

            <div className="form-check mb-4">
              <input
                id="bios-installed-check"
                type="checkbox"
                checked={biosInstalled}
                onChange={handleBiosToggle}
                className="form-check-input"
              />
              <label
                htmlFor="bios-installed-check"
                className="form-check-label fs-5 fw-medium text-dark cursor-pointer"
              >
                ✅ 조립 및 BIOS 설치 완료 확인
              </label>
            </div>

            <button
              onClick={() => handleAssemblyStatusChange("SHIPPING_WAIT")}
              disabled={!biosInstalled || isLoading}
              className={`btn btn-warning btn-lg w-100 fw-bold d-flex align-items-center justify-content-center ${
                !biosInstalled || isLoading
                  ? "opacity-75 disabled"
                  : "shadow-sm"
              }`}
            >
              {isLoading ? (
                <IoReloadCircleOutline
                  className="spin me-2"
                  width="20"
                  height="20"
                />
              ) : (
                <IoArrowForward className="me-2" width="20" height="20" />
              )}
              조립 완료 확인 &rarr; 운송장 등록 대기
            </button>
          </div>
        );

      case "SHIPPING_WAIT":
        return (
          <div className="card p-3 border-info bg-info-subtle">
            <p className="card-title fw-semibold text-info-emphasis d-flex align-items-center mb-3">
              <IoCarOutline className="me-2" width="18" height="18" />
              운송장 번호 입력
            </p>

            <div className="mb-3">
              <input
                type="text"
                value={trackingInfo.trackingNumber}
                onChange={handleTrackingNumber}
                placeholder="운송장 번호를 입력하세요 (예: 1234-5678-9012)"
                className="form-control"
                disabled={isLoading}
              />
            </div>
            <button
              className="btn btn-secondary dropdown-toggle d-flex align-items-center justify-content-center"
              type="button"
              data-bs-toggle="dropdown"
              aria-expanded="false"
              disabled={isLoading}
            >
              {CARREIRS.find((c) => c.id === trackingInfo.carrierId)?.name ||
                "택배사 선택"}
            </button>

            <ul className="dropdown-menu">
              {CARREIRS.map((carrier) => (
                <li key={carrier.id}>
                  <a
                    className={`dropdown-item ${
                      trackingInfo.carrierId === carrier.id ? "active" : ""
                    }`}
                    href="#"
                    onClick={(e) => {
                      e.preventDefault(); // 기본 링크 동작 방지
                      handleCarrierId(carrier.id);
                    }}
                  >
                    {carrier.name}
                  </a>
                </li>
              ))}
            </ul>
            <button
              onClick={() => handleAssemblyStatusChange("COMPLETED")}
              disabled={!trackingInfo.trackingNumber.trim() || isLoading}
              className={`btn btn-info btn-lg w-100 fw-bold d-flex align-items-center justify-content-center ${
                !trackingInfo.trackingNumber.trim() || isLoading
                  ? "opacity-75 disabled"
                  : "shadow-sm"
              }`}
            >
              {isLoading ? (
                <IoReloadCircleOutline
                  className="spin me-2"
                  width="20"
                  height="20"
                />
              ) : (
                <IoCheckmarkCircleOutline
                  className="me-2"
                  width="20"
                  height="20"
                />
              )}
              운송장 등록 완료 &rarr; 최종 출고
            </button>
          </div>
        );

      case "COMPLETED":
        return (
          <div
            className="alert alert-success p-4 rounded shadow-sm"
            role="alert"
          >
            <p className="lead fw-bold text-success d-flex align-items-center justify-content-center mb-1">
              <IoCheckmarkCircleOutline
                className="me-2"
                width="24"
                height="24"
              />
              PC 출고 작업 완료!
            </p>
            <p className="text-center small text-secondary mb-0">
              최종 운송장 번호:{" "}
              <span className="font-monospace text-success fw-bold">
                {trackingInfo.trackingNumber}
              </span>
            </p>
          </div>
        );

      default:
        return null;
    }
  };

  // 현재 상태 카드 헤더 렌더링
  const renderStatusHeader = () => {
    const { icon: Icon, color, text } = getStatusProps(order.assemblyStatus);
    return (
      <div className={`p-3 rounded d-flex align-items-center ${color}`}>
        <Icon width="28" height="28" className="me-3" />
        <h2 className="h5 fw-bold mb-0">{text}</h2>
      </div>
    );
  };

  return (
    <div className="min-vh-100 bg-light p-3 p-sm-5">
      <style>{`
        .spin { animation: spin 1s linear infinite; }
        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
        .h-1 { height: 0.25rem !important; }
      `}</style>
      <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

      <div className="container-md mx-auto">
        <h1 className="h3 fw-bolder text-dark mb-2">
          PC 조립/출고 관리 대시보드
        </h1>
        <p className="text-secondary mb-4">
          주문 ID:{" "}
          <span className="font-monospace fw-semibold text-dark">
            {order.orderId}
          </span>{" "}
          / 고객명:{" "}
          <span className="fw-semibold text-dark">
            {order.customer.customerName}
          </span>
        </p>

        {/* 1. 워크플로우 진행 바 */}
        <WorkflowStep status={order.assemblyStatus} />

        {/* 2. 현재 상태 요약 및 액션 카드 (Bootstrap Grid System) */}
        <div className="row g-4 mb-4">
          {/* 상태 카드 */}
          <div className="col-lg-4">
            <div className="card shadow-sm border-0 h-100">
              <div className="card-body">
                <h3 className="h5 fw-bold mb-3 text-dark">현재 작업 상태</h3>
                {renderStatusHeader()}

                <div className="mt-3 pt-3 border-top small text-secondary">
                  {order.status !== "COMPLETED" && (
                    <p className="mb-1">
                      <span className="fw-medium">필수 S/N 입력:</span>{" "}
                      {isSnComplete ? "✅ 완료" : "❌ 미완료"}
                    </p>
                  )}
                  <p className="mb-0">
                    <span className="fw-medium">BIOS/OS 설치:</span>{" "}
                    {order.biosInstalled ? "✅ 완료" : "❌ 미완료"}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* 액션 패널 */}
          <div className="col-lg-8">
            <div className="card shadow-sm border-0 h-100">
              <div className="card-body">
                <h3 className="h5 fw-bold mb-3 text-dark">다음 진행 단계</h3>
                {renderActionButton()}
              </div>
            </div>
          </div>
        </div>

        {/* 3. 부품 목록 및 S/N 입력/확인 테이블 */}
        <div className="card shadow-sm border-0">
          <div className="card-body">
            <h3 className="h5 fw-bold text-dark mb-3 d-flex align-items-center">
              <IoClipboardOutline
                className="me-2 text-primary"
                width="24"
                height="24"
              />
              주문 부품 목록 및 S/N 기록
            </h3>

            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th className="py-3 text-start small text-secondary">
                      카테고리
                    </th>
                    <th className="py-3 text-start small text-secondary">
                      모델명
                    </th>
                    <th className="py-3 text-start small text-secondary w-25">
                      일련번호 (S/N)
                    </th>
                    <th className="py-3 text-start small text-secondary">
                      필수 여부
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {order.orderItems.map((orderItem) => (
                    <tr
                      key={orderItem.orderItemId}
                      className={`${
                        orderItem.serialNumRequired ? "" : "table-light"
                      }`}
                    >
                      <td className="py-3 fw-medium text-dark small">
                        {orderItem.itemCategory}
                      </td>
                      <td className="py-3 text-secondary small font-monospace">
                        {orderItem.itemName}
                      </td>
                      <td className="py-3 small">
                        {order.assemblyStatus === "INSPECTING" ? (
                          <input
                            type="text"
                            value={orderItem.serialNum || ""}
                            onChange={(e) =>
                              handleSnChange(
                                orderItem.orderItemId,
                                e.target.value
                              )
                            }
                            placeholder="S/N 스캔 또는 입력"
                            className="form-control form-control-sm"
                          />
                        ) : (
                          <span
                            className={`font-monospace ${
                              orderItem.serialNum
                                ? "text-dark"
                                : "text-muted fst-italic"
                            }`}
                          >
                            {orderItem.serialNum ||
                              (orderItem.serialNumRequired
                                ? "필수 입력 대기"
                                : "선택 사항")}
                          </span>
                        )}
                      </td>
                      <td className="py-3 small">
                        <span
                          className={`badge ${
                            orderItem.serialNumRequired
                              ? "text-bg-danger"
                              : "text-bg-success"
                          }`}
                        >
                          {orderItem.serialNumRequired ? "필수" : "선택"}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AssemblyDetail;
