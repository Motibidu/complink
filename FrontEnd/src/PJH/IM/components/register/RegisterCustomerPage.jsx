import React, { useState, useCallback, useEffect } from "react";
import axios from "axios"; // axios import 추가
import qs from "qs";

const CustomerFormPage = () => {
  // 폼 데이터를 한 번에 관리하기 위한 state
  const [customers, setCustomers] = useState([]);
  const [selectedCustomers, setSelectedCustomers] = useState([]);
  const [newFormData, setNewFormData] = useState({
    customerId: "CUST-1000",
    customerName: "리보시스템즈",
    phoneNumber: "010-1111-2222",
    email: "kmwon@example.com",
    address: "서울시 강남구 테헤란로 1",
  });

  const [editFormData, setEditFormData] = useState({
    customerId: "",
    customerName: "",
    phoneNumber: "",
    email: "",
    address: "",
  });

  // API 요청 상태(로딩)를 관리하는 state
  const [loading, setLoading] = useState(false);
  // 사용자에게 보여줄 메시지(성공/실패)를 관리하는 state
  const [message, setMessage] = useState({ type: "", text: "" });

  const fetchCustomers = useCallback(async () => {
    try {
      const response = await axios.get("/api/customers");
      setCustomers(response.data);
    } catch (error) {
      console.error("거래처 목록을 불러오는 데 실패했습니다.", error);
      setMessage({
        type: "danger",
        text: "데이터를 불러오는 데 실패했습니다.",
      });
    }
  }, []);

  useEffect(() => {
    fetchCustomers();
  }, [fetchCustomers]);

  //handle select
  const handleSelectAll = (e) => {
    if (e.target.checked) {
      const allCustomerIds = customers.map((customer) => customer.customerId);
      setSelectedCustomers(allCustomerIds);
    } else {
      setSelectedCustomers([]);
    }
  };

  const handleSelectCustomer = (customerId) => {
    if (selectedCustomers.includes(customerId)) {
      setSelectedCustomers((prevSelected) =>
        prevSelected.filter((id) => id !== customerId)
      );
    } else {
      setSelectedCustomers((prevSelected) => [...prevSelected, customerId]);
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedCustomers.length === 0) {
      alert("삭제할 거래처를 선택해주세요.");
      return;
    }

    if (
      window.confirm(
        `선택된 ${selectedCustomers.length}개의 거래처를 정말 삭제하시겠습니까?`
      )
    ) {
      try {
        await axios.delete("/api/customers", {
          params: {
            ids: selectedCustomers
          },
          paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'comma' })
          }

        });

        alert("선택된 거래처가 삭제되었습니다.");
        fetchCustomers();
        setSelectedCustomers([]);
      } catch (error) {
        console.error("거래처 삭제 중 오류 발생:", error);
        alert("삭제 중 오류가 발생했습니다.");
      }
    }
  };

  // handle changes
  const handleNewFormChange = (e) => {
    const { name, value } = e.target;
    setNewFormData((prevState) => ({
      ...prevState,
      [name]: value,
    }));
  };

  const handleEditFormChange = (e) => {
    const { name, value } = e.target;
    setEditFormData((prevState) => ({ ...prevState, [name]: value }));
  };

  const handleNewFormSubmit = async (e) => {
    e.preventDefault(); // 폼의 기본 새로고침 동작 방지

    if (!newFormData.customerName) {
      setMessage({
        type: "danger",
        text: "거래처 코드와 거래처명은 필수 항목입니다.",
      });
      return;
    }

    setLoading(true);
    setMessage({ type: "", text: "" });

    try {
      console.log(newFormData);
      const response = await axios.post("/api/customers", newFormData);

      if (response.status === 201 || response.status === 200) {
        setNewFormData({
          customerId: "",
          customerName: "",
          phoneNumber: "",
          email: "",
          address: "",
        });
        fetchCustomers();
        const modalElement = document.getElementById("editFormModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message || "거래처 등록 중 오류가 발생했습니다.";
      setMessage({ type: "danger", text: errorMsg });
      console.error("Error submitting form:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditFormSubmit = async (e) => {
    e.preventDefault();

    if (
      !editFormData.customerName || !editFormData.phoneNumber|| !editFormData.email|| !editFormData.address
    ) {
      setMessage({ type: "danger", text: "필수 항목(*)을 모두 입력해주세요." });
      return;
    }

    setLoading(true);
    setMessage({ type: "", text: "" });

    try {
      const response = await axios.put(
        "/api/customers/" + editFormData.customerId,
        editFormData
      );

      if (response.status === 201 || response.status===200) {
        // This resets the edit form, which might not be needed if the modal closes.
        setEditFormData({
          customerName: "",
          phoneNumber: "",
          email: "",
          address: "",
        });
        fetchCustomers();
        const modalElement = document.getElementById("editFormModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
        alert("선택한 거래처의 수정이 완료되었습니다.");
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message || "거래처 수정 중 오류가 발생했습니다.";
      setMessage({ type: "danger", text: errorMsg });
      console.error("Error submitting form:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditClick = (customerToEdit) => {
    setEditFormData({
      customerId: customerToEdit.customerId || "",
      customerName: customerToEdit.customerName || "",
      phoneNumber: customerToEdit.phoneNumber || "",
      email: customerToEdit.email || "",
      address: customerToEdit.address || "",
    });
  };
  return (
    <>
      <header className="mb-3">
        <h3>거래처등록 리스트</h3>
      </header>
      <div className="table-responsive table-container-scrollable">
        <table className="table table-hover align-middle">
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  onChange={handleSelectAll}
                  checked={
                    customers.length > 0 &&
                    selectedCustomers.length === customers.length
                  }
                />
              </th>
              <th>거래처 코드</th>
              <th>이름</th>
              <th>전화번호</th>
              <th>이메일</th>
              <th>주소</th>
            </tr>
          </thead>
          <tbody>
            {customers.map((customer) => (
              <tr key={customer.customerId}>
                <td>
                  <input
                    type="checkbox"
                    checked={selectedCustomers.includes(customer.customerId)}
                    onChange={() => handleSelectCustomer(customer.customerId)}
                  />
                </td>
                <td>{customer.customerId}</td>
                <td>
                  <a
                    onClick={() => handleEditClick(customer)}
                    href="#"
                    data-bs-toggle="modal"
                    data-bs-target="#editFormModal"
                  >
                    {customer.customerName}
                  </a>
                </td>
                <td>{customer.phoneNumber}</td>
                <td>{customer.email}</td>
                <td>{customer.address}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <footer className="mt-3">
        <button
          className="btn btn-primary mx-3"
          data-bs-toggle="modal"
          data-bs-target="#newFormModal"
        >
          신규 거래처 등록
        </button>
        <button className="btn btn-primary me-3" onClick={handleDeleteSelected}>
          삭제
        </button>
      </footer>
      <div
        className="modal fade"
        id="newFormModal"
        tabIndex="-1"
        aria-labelledby="newFormModal"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleNewFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="newFormModal">
                  신규 거래처 등록
                </h1>
                <button
                  type="button"
                  className="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                
                <div className="col-md-6">
                  <label htmlFor="customerName" className="form-label">
                    거래처명 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="customerName"
                    name="customerName"
                    value={newFormData.customerName}
                    onChange={handleNewFormChange}
                    required
                  />
                </div>

                {/* 연락처 */}
                <div className="col-md-6">
                  <label htmlFor="phoneNumber" className="form-label">
                    연락처
                  </label>
                  <input
                    type="tel"
                    className="form-control"
                    id="phoneNumber"
                    name="phoneNumber"
                    value={newFormData.phoneNumber}
                    onChange={handleNewFormChange}
                    placeholder="010-1234-5678"
                  />
                </div>

                {/* 이메일 */}
                <div className="col-12">
                  <label htmlFor="email" className="form-label">
                    이메일
                  </label>
                  <input
                    type="email"
                    className="form-control"
                    id="email"
                    name="email"
                    value={newFormData.email}
                    onChange={handleNewFormChange}
                    placeholder="example@company.com"
                  />
                </div>

                {/* 주소 */}
                <div className="col-12">
                  <label htmlFor="address" className="form-label">
                    주소
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="address"
                    name="address"
                    value={newFormData.address}
                    onChange={handleNewFormChange}
                  />
                </div>
              </div>
              {/* 메시지 표시 영역 */}
              {message.text && (
                <div
                  className={`alert alert-${message.type} mt-4`}
                  role="alert"
                >
                  {message.text}
                </div>
              )}

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
      <div
        className="modal fade"
        id="editFormModal"
        tabIndex="-1"
        aria-labelledby="editFormModal"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleEditFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="editFormModal">
                  거래처 정보 수정
                </h1>
                <button
                  type="button"
                  className="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                
                <div className="col-md-6">
                  <label htmlFor="customerName" className="form-label">
                    거래처명 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="customerName"
                    name="customerName"
                    value={editFormData.customerName}
                    onChange={handleEditFormChange}
                    required
                  />
                </div>

                {/* 연락처 */}
                <div className="col-md-6">
                  <label htmlFor="phoneNumber" className="form-label">
                    연락처
                  </label>
                  <input
                    type="tel"
                    className="form-control"
                    id="phoneNumber"
                    name="phoneNumber"
                    value={editFormData.phoneNumber}
                    onChange={handleEditFormChange}
                    placeholder="010-1234-5678"
                  />
                </div>

                {/* 이메일 */}
                <div className="col-12">
                  <label htmlFor="email" className="form-label">
                    이메일
                  </label>
                  <input
                    type="email"
                    className="form-control"
                    id="email"
                    name="email"
                    value={editFormData.email}
                    onChange={handleEditFormChange}
                    placeholder="example@company.com"
                  />
                </div>

                {/* 주소 */}
                <div className="col-12">
                  <label htmlFor="address" className="form-label">
                    주소
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="address"
                    name="address"
                    value={editFormData.address}
                    onChange={handleEditFormChange}
                  />
                </div>
              </div>
              {/* 메시지 표시 영역 */}
              {message.text && (
                <div
                  className={`alert alert-${message.type} mt-4`}
                  role="alert"
                >
                  {message.text}
                </div>
              )}

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
    </>
  );
};

export default CustomerFormPage;
