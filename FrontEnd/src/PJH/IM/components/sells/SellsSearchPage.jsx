import React, { useState, useEffect, useMemo, useCallback } from "react";
import axios from "axios";

// const fetchTrackingStatus = async (orderId) => {
//   try {
//     await axios.get(`/api/delivery/registered/${orderId}`);
//     return true;
//   } catch (error) {
//     return false;
//   }
// };
const fetchDelivery = async (orderId) => {
  try {
    const resp = await axios.get(`/api/delivery/${orderId}`);
    return resp.data;
  } catch (err) {
    return null;
    //console.err(err);
  }
};
const SellsSearchPage = () => {
  const [sells, setsells] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState({ type: "", text: "" });

  // 필터링을 위한 state
  const [searchTerm, setSearchTerm] = useState("");
  const [dateRange, setDateRange] = useState({
    start: "",
    end: new Date().toISOString().slice(0, 10), // 오늘 날짜를 기본값으로 설정
  });
  const [TrackingNumberReq, setTrackingNumberReq] = useState({
    orderId: "",
    customerId: "",
    trackingNumber: "508368319105",
    carrierId: "kr.cjlogistics",
  });

  // 상세 조회를 위한 state
  const [selectedsell, setSelectedsell] = useState(null);

  const openTrackingNumberReqModal = (sell) => {
    // 선택된 판매의 orderId를 설정하고 운송장 정보는 초기화 (새로 입력해야 하므로)
    setTrackingNumberReq({
      orderId: sell.orderId,
      customerId: sell.customerId,
      trackingNumber: TrackingNumberReq.trackingNumber,
      carrierId: TrackingNumberReq.carrierId,
    });
    setMessage({ type: "", text: "" }); // 메시지 초기화
  };

  const openDeliveryDetailModal = (sell) => {
    setSelectedsell(sell);
  };

  const fetchSells = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const sellsResponse = await axios.get("/api/sells");
      const sellsData = sellsResponse.data;

      // 💡 각 주문에 대한 배송 추적 등록 상태(Delivery 레코드 존재 여부)를 병렬로 확인
      const statusPromises = sellsData.map((sell) =>
        fetchDelivery(sell.orderId).then((delivery) => ({
          ...sell,
          delivery: delivery,
        }))
      );

      // 모든 상태 확인이 완료될 때까지 기다림
      const enrichedSells = await Promise.all(statusPromises);

      console.log("Enriched Sells Data:", enrichedSells);
      setsells(enrichedSells);
    } catch (err) {
      setError("판매 데이터를 불러오는 데 실패했습니다.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSells();
  }, [fetchSells]);

  // 검색어와 날짜 범위에 따라 데이터를 필터링 (useMemo로 성능 최적화)
  const filteredsells = useMemo(() => {
    return sells.filter((sell) => {
      // sellDate는 그대로 둡니다.
      const sellDate = new Date(sell.sellDate);

      const startDate = dateRange.start ? new Date(dateRange.start) : null;

      // 👇 endDate를 설정할 때, 그날의 가장 마지막 시간으로 설정합니다.
      let endDate = null;
      if (dateRange.end) {
        endDate = new Date(dateRange.end);
        endDate.setHours(23, 59, 59, 999); // 해당 날짜의 23시 59분 59초로 설정
      }

      // 이제 날짜 비교가 정확하게 동작합니다.
      if (startDate && sellDate < startDate) return false;
      if (endDate && sellDate > endDate) return false;

      const lowercasedSearchTerm = searchTerm.toLowerCase();

      return (
        sell.customerName?.toLowerCase().includes(lowercasedSearchTerm) ||
        sell.managerName?.toLowerCase().includes(lowercasedSearchTerm)
      );
    });
  }, [sells, searchTerm, dateRange]);
  //console.log("filteredsells: ", filteredsells);
  // 필터링된 데이터의 합계 계산
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
    setSelectedsell(sell);
  };

  const handleWaybillFormChange = (e) => {
    const { name, value } = e.target;
    setTrackingNumberReq((prevState) => ({
      ...prevState,
      [name]: value,
    }));
  };
  const trackingNumberReqSubmit = async (e) => {
    e.preventDefault(); // 폼의 기본 새로고침 동작 방지

    setLoading(true);

    try {
      console.log("TrackingNumberReq: ", TrackingNumberReq);
      const response = await axios.post(
        "/api/delivery/trackingNumber",
        TrackingNumberReq
      );
      console.log("response: ", response);

      if (response.status === 201 || response.status === 200) {
        setTrackingNumberReq({
          trackingNumber: "",
          carrierId: "",
        });
        const modalElement = document.getElementById("trackingNumberReqModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }

        fetchSells();

        alert(response.data);
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message ||
        "운송장 번호 등록 중 오류가 발생했습니다.";
      setMessage({ type: "danger", text: errorMsg });
    } finally {
      setLoading(false);
    }
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
                  로딩 중...
                </td>
              </tr>
            ) : filteredsells.length > 0 ? (
              filteredsells.map((sell) => {
                // isRegistered 값에 따라 버튼의 텍스트와 클래스를 미리 정의
                // const isRegistered = sell.delivery.trackingNumber; // 가정: 운송장 번호가 있으면 등록된 상태

                // const buttonText = isRegistered
                //   ? "[" + sell.delivery.currentStatus + "/상세보기]" // 등록 완료: 현재 배송 상태 표시
                //   : "[운송장번호 입력/ 배송추적]"; // 미등록: 입력 요청 텍스트 표시

                // const buttonClass = isRegistered
                //   ? "btn-outline-primary" // 등록 완료: 초록색/비활성화
                //   : "btn-outline-secondary"; // 미등록: 파란색 테두리/활성화

                // const modalTarget = isRegistered
                //   ? "#deliveryDatailModal"
                //   : "#trackingNumberReqModal";
                return (
                  <tr
                    key={sell.sellId}
                    onClick={() => handleViewDetails(sell)}
                    data-bs-toggle="modal"
                    data-bs-target="#sellDetailModal"
                    style={{ cursor: "pointer" }}
                  >
                    <td>{sell.sellDate.split("T")[0]}</td>
                    <td>{sell.orderId}</td>
                    <td>{sell.sellId}</td>
                    <td>{sell.customerName}</td>
                    <td>{sell.managerName || "-"}</td>
                    <td className="text-end">
                      {sell.grandAmount.toLocaleString()}원
                    </td>
                    {/* <td>
                      <button
                        className={`btn btn-sm ${buttonClass}`}
                        data-bs-target={modalTarget}
                        data-bs-toggle="modal"
                        onClick={(e) => {
                          e.stopPropagation();
                          if (!isRegistered) {
                            openTrackingNumberReqModal(sell);
                          } else {
                            openDeliveryDetailModal(sell);
                          }
                        }}
                      >
                        {buttonText}
                      </button>
                    </td> */}
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
                조회된 합계
              </td>
              <td className="text-end">
                {totals.grandAmount.toLocaleString()}원
              </td>
            </tr>
          </tfoot>
        </table>
      </div>
      <div className="modal fade" id="trackingNumberReqModal" tabIndex="-1">
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
                <button type="submit" className="btn btn-primary">
                  저장하기
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div className="modal fade" id="sellDetailModal" tabIndex="-1">
        <div className="modal-dialog modal-lg">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title" id="sellDetailModalLabel">
                판매 상세 정보 (판매번호: {selectedsell?.sellId})
              </h5>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>
            <div className="modal-body">
              {selectedsell ? (
                <div>
                  <p>
                    <strong>판매일:</strong>{" "}
                    {selectedsell.sellDate.split("T")[0]}
                  </p>
                  <p>
                    <strong>거래처:</strong> {selectedsell.customerName} (
                    {selectedsell.customerId})
                  </p>
                  <p>
                    <strong>담당자:</strong>{" "}
                    {selectedsell.managerName || "미지정"}
                  </p>
                  <hr />
                  <p>
                    <strong>공급가액:</strong>{" "}
                    {selectedsell.totalAmount.toLocaleString()}원
                  </p>
                  <p>
                    <strong>부가세:</strong>{" "}
                    {selectedsell.vatAmount.toLocaleString()}원
                  </p>
                  <p>
                    <strong>총 합계:</strong>{" "}
                    {selectedsell.grandAmount.toLocaleString()}원
                  </p>
                  {/* <p>
                    <strong>결제 상태:</strong> {selectedsell.paymentStatus}
                  </p> */}
                  <p>
                    <strong>원본 주문번호:</strong> {selectedsell.orderId}
                  </p>
                  <p>
                    <strong>메모:</strong> {selectedsell.memo || "없음"}
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
      {/* <div className="modal fade" id="deliveryDatailModal" tabIndex="-1">
        <div className="modal-dialog modal-lg">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title" id="sellDetailModalLabel">
                배송 상세 정보
              </h5>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>
            <div className="modal-body">
              {selectedsell ? (
                <div>
                  <p>
                    <strong>수령인 이름:</strong>{" "}
                    {selectedsell.delivery.recipientName}
                  </p>
                  <p>
                    <strong>수령인 주소: </strong>
                    {selectedsell.delivery.recipientAddr}
                  </p>
                  <p>
                    <strong>수령인 전화번호:</strong>{" "}
                    {selectedsell.delivery.recipientPhone}
                  </p>
                  <p>
                    <strong>배송 상태:</strong>{" "}
                    {selectedsell.delivery.currentStatus}
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
      </div> */}
    </div>
  );
};

export default SellsSearchPage;
