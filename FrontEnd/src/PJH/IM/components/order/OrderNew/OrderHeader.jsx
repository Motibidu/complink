import { useState, useMemo } from "react";
import { IoReorderThree, IoReorderFourOutline } from "react-icons/io5";
const OrderHeader = ({ orderHeader, handleHeaderChange }) => {
  const [customers, setCustomers] = useState([]);
  const [managers, setManagers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");

  const fetchCustomers = async () => {
    setLoading(true);
    try {
      const response = await fetch("/api/order/findAllCustomers");
      const data = await response.json();
      setCustomers(data);
    } catch (err) {
      console.error("거래처 목록 로드 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  const fetchManagers = async () => {
    setLoading(true);
    try {
      const response = await fetch("/api/order/findAllManagers");
      const data = await response.json();
      console.log(data);
      setManagers(data);
    } catch (err) {
      console.err("담당자 목록 로드 실패: ", err);
    } finally {
      setLoading(false);
    }
  };

  const handleCustomerSelect = (customer) => {
    // 부모 컴포넌트의 상태를 업데이트
    handleHeaderChange({
      target: { name: "customerId", value: customer.customerId },
    });
    handleHeaderChange({
      target: { name: "customerName", value: customer.customerName },
    });
  };

  const handleManagerSelect = (manager) => {
    // 부모 컴포넌트의 상태를 업데이트
    handleHeaderChange({
      target: { name: "managerId", value: manager.managerId },
    });
    handleHeaderChange({
      target: { name: "managerName", value: manager.managerName },
    });
  };

  // 검색어에 따라 고객 목록 필터링
  const filteredCustomers = useMemo(() => {
    if (!customers) return [];
    return customers.filter(
      (customer) =>
        customer.customerName
          .toLowerCase()
          .includes(searchTerm.toLowerCase()) ||
        customer.customerId.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [customers, searchTerm]);

  const filteredManagers = useMemo(() => {
    if (!managers) return [];
    return managers.filter(
      (managers) =>
        managers.managerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        managers.managerId.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [managers, searchTerm]);
  return (
    <>
      <div className="orderHeader">
        <h3 className="orderHeader__title">주문 기본 정보</h3>
        <div className="orderHeader__group">
          <label className="orderHeader__label">
            일자:
            <input
              className="orderHeader__input"
              type="date"
              name="orderDate"
              value={orderHeader.orderDate}
              onChange={handleHeaderChange}
            />
          </label>
          <label className="orderHeader__label">
            <span>
              담당자
              <IoReorderFourOutline
                onClick={fetchManagers}
                className="orderHeader__list"
                size={25}
                data-bs-toggle="modal"
                data-bs-target="#managerListModal"
                readOnly
              />
            </span>
            <input
              className="orderHeader__input"
              type="text"
              name="managerId"
              value={orderHeader.managerId}
              onChange={handleHeaderChange}
              placeholder="담당자Id"
              onClick={fetchManagers}
              data-bs-toggle="modal"
              data-bs-target="#managerListModal"
              readOnly
            />
            <input
              className="orderHeader__input"
              type="text"
              name="managerName"
              value={orderHeader.managerName}
              onChange={handleHeaderChange}
              placeholder="담당자명"
              onClick={fetchManagers}
              data-bs-toggle="modal"
              data-bs-target="#managerListModal"
              readOnly
            />
          </label>
          <label className="orderHeader__label">
            <span>
              거래처
              <IoReorderFourOutline
                className="orderHeader__list"
                size={25}
                onClick={fetchCustomers} // 아이콘 클릭 시 데이터 로드
                data-bs-toggle="modal"
                data-bs-target="#customerListModal"
              />
            </span>
            <input
              className="orderHeader__input"
              type="text"
              name="customerId"
              value={orderHeader.customerId}
              onChange={handleHeaderChange}
              placeholder="거래처 코드"
              onClick={fetchCustomers} // 아이콘 클릭 시 데이터 로드
              data-bs-toggle="modal"
              data-bs-target="#customerListModal"
              readOnly // 모달에서 선택하도록 읽기 전용으로
            />
            <input
              className="orderHeader__input"
              type="text"
              name="customerName"
              value={orderHeader.customerName}
              onChange={handleHeaderChange}
              placeholder="거래처명"
              onClick={fetchCustomers} // 아이콘 클릭 시 데이터 로드
              data-bs-toggle="modal"
              data-bs-target="#customerListModal"
              readOnly
            />
          </label>
          <label className="orderHeader__label">
            납기일:
            <input
              className="orderHeader__input"
              type="date"
              name="deliveryDate"
              value={orderHeader.deliveryDate}
              onChange={handleHeaderChange}
            />
          </label>
        </div>
      </div>

      <div
        className="modal fade"
        id="customerListModal"
        tabIndex="-1"
        aria-labelledby="customerModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-scrollable">
          {" "}
          {/* 스크롤 가능하도록 클래스 추가 */}
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5" id="customerModalLabel">
                거래처 선택
              </h1>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>

            {/* 모달 Body 수정 */}
            <div className="modal-body">
              {/* 1. 검색창 추가 */}
              <div className="mb-3">
                <input
                  type="text"
                  className="form-control"
                  placeholder="거래처 코드 또는 이름으로 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
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
              {!loading && filteredCustomers.length > 0 && (
                <div className="list-group customer-list">
                  {filteredCustomers.map((customer) => (
                    <button
                      type="button"
                      key={customer.customerId}
                      className="list-group-item list-group-item-action"
                      data-bs-dismiss="modal"
                      onClick={() => handleCustomerSelect(customer)}
                    >
                      <div className="d-flex w-100 justify-content-between">
                        <h6 className="mb-1 fw-bold">
                          {customer.customerName}
                        </h6>
                        <small className="text-muted">
                          {customer.customerId}
                        </small>
                      </div>
                      <p className="mb-1 text-muted small">
                        {customer.address}
                      </p>
                    </button>
                  ))}
                </div>
              )}

              {/* 4. 데이터가 없거나, 검색 결과가 없을 때 */}
              {!loading && filteredCustomers.length === 0 && (
                <div className="text-center text-muted py-5">
                  {customers.length === 0
                    ? "등록된 거래처가 없습니다."
                    : "검색 결과가 없습니다."}
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
            </div>
          </div>
        </div>
      </div>
      <div
        className="modal fade"
        id="managerListModal"
        tabIndex="-1"
        aria-labelledby="managerModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-scrollable">
          {" "}
          {/* 스크롤 가능하도록 클래스 추가 */}
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5" id="managerModalLabel">
                담당자 선택
              </h1>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>

            {/* 모달 Body 수정 */}
            <div className="modal-body">
              {/* 1. 검색창 추가 */}
              <div className="mb-3">
                <input
                  type="text"
                  className="form-control"
                  placeholder="담당자 코드 또는 이름으로 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
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
              {!loading && filteredManagers.length > 0 && (
                <div className="list-group manager-list">
                  {filteredManagers.map((manager) => (
                    <button
                      type="button"
                      key={manager.customerId}
                      className="list-group-item list-group-item-action"
                      data-bs-dismiss="modal"
                      onClick={() => handleManagerSelect(manager)}
                    >
                      <div className="d-flex w-100 justify-content-between">
                        <h6 className="mb-1 fw-bold">{manager.managerName}</h6>
                        <small className="text-muted">
                          {manager.managerId}
                        </small>
                      </div>
                      <p className="mb-1 text-muted small">{manager.email}</p>
                    </button>
                  ))}
                </div>
              )}

              {/* 4. 데이터가 없거나, 검색 결과가 없을 때 */}
              {!loading && filteredManagers.length === 0 && (
                <div className="text-center text-muted py-5">
                  {managers.length === 0
                    ? "등록된 담당자가 없습니다."
                    : "검색 결과가 없습니다."}
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
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default OrderHeader;
