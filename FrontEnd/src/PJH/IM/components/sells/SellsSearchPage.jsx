import React, { useState, useEffect, useMemo, useCallback } from "react";
import axios from "axios";

const SellsSearchPage = () => {
  const [sells, setsells] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // 필터링을 위한 state
  const [searchTerm, setSearchTerm] = useState("");
  const [dateRange, setDateRange] = useState({
    start: "",
    end: new Date().toISOString().slice(0, 10), // 오늘 날짜를 기본값으로 설정
  });

  // 상세 조회를 위한 state
  const [selectedsell, setSelectedsell] = useState(null);

  const fetchsells = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await axios.get("/api/sells");
      console.log("response.data: ", response.data);
      setsells(response.data);
    } catch (err) {
      setError("판매 데이터를 불러오는 데 실패했습니다.");
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchsells();
  }, [fetchsells]);

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
  console.log("filteredsells: ", filteredsells);
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
  
  // 상세 조회 모달을 열기 위한 함수
  const handleViewDetails = (sell) => {
    setSelectedsell(sell);
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
              <th>판매번호</th>
              <th>거래처명</th>
              <th>담당자명</th>
              <th className="text-end">합계 금액</th>
              <th>결제 상태</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan="6" className="text-center">
                  로딩 중...
                </td>
              </tr>
            ) : filteredsells.length > 0 ? (
              filteredsells.map((sell) => (
                <tr key={sell.sellId} onClick={() => handleViewDetails(sell)} data-bs-toggle="modal"
                data-bs-target="#sellDetailModal" style={{ cursor: 'pointer' }}>
                  <td>{sell.sellDate.split("T")[0]}</td>
                  <td>{sell.sellId}</td>
                  <td>{sell.customerName}</td>
                  <td>{sell.managerName || "-"}</td>
                  <td className="text-end">
                    {sell.grandAmount.toLocaleString()}원
                  </td>
                  <td>
                    <span className="badge bg-success">{sell.paymentStatus}</span>
                  </td>
                </tr>
              ))
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
              <td colSpan="4" className="text-end">
                조회된 합계
              </td>
              <td className="text-end">{totals.grandAmount.toLocaleString()}원</td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>
       {/* 상세 조회 모달 */}
       <div className="modal fade" id="sellDetailModal" tabIndex="-1" aria-labelledby="sellDetailModalLabel" aria-hidden="true">
        <div className="modal-dialog modal-lg">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title" id="sellDetailModalLabel">
                판매 상세 정보 (판매번호: {selectedsell?.sellId})
              </h5>
              <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div className="modal-body">
              {selectedsell ? (
                <div>
                  <p><strong>판매일:</strong> {selectedsell.sellDate.split("T")[0]}</p>
                  <p><strong>거래처:</strong> {selectedsell.customerName} ({selectedsell.customerId})</p>
                  <p><strong>담당자:</strong> {selectedsell.managerName || "미지정"}</p>
                  <hr />
                  <p><strong>공급가액:</strong> {selectedsell.totalAmount.toLocaleString()}원</p>
                  <p><strong>부가세:</strong> {selectedsell.vatAmount.toLocaleString()}원</p>
                  <p><strong>총 합계:</strong> {selectedsell.grandAmount.toLocaleString()}원</p>
                  <p><strong>결제 상태:</strong> {selectedsell.paymentStatus}</p>
                  <p><strong>원본 주문번호:</strong> {selectedsell.orderId}</p>
                  <p><strong>메모:</strong> {selectedsell.memo || "없음"}</p>
                </div>
              ) : (
                <p>상세 정보를 불러올 수 없습니다.</p>
              )}
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SellsSearchPage;