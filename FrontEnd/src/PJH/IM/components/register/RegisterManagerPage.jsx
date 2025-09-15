import React, { useState, useEffect, useCallback } from "react";
import "./RegisterManagerPage.css";
import axios from "axios";
import qs from "qs";

const ManagerPage = () => {
  const [managers, setManagers] = useState([]);
  const [selectedManagers, setSelectedManagers] = useState([]);
  const [newFormData, setNewFormData] = useState({
    managerId: "MGR-0025",
    managerName: "김경원",
    email: "kmwon@example.com",
    phoneNumber: "01011112222",
  });

  const [editFormData, setEditFormData] = useState({
    managerId: "",
    managerName: "",
    email: "",
    phoneNumber: "",
  });

  // 로딩 및 메시지 상태
  const [tableLoading, setTableLoading] = useState(true);
  const [formLoading, setFormLoading] = useState(false);
  const [message, setMessage] = useState({ type: "", text: "" });

  // --- 데이터 처리 함수 ---

  // 백엔드에서 담당자 목록을 불러오는 함수
  const fetchManagers = useCallback(async () => {
    setTableLoading(true);
    try {
      const response = await axios.get("/api/managers");
      setManagers(response.data);
    } catch (error) {
      console.error("담당자 목록을 불러오는 데 실패했습니다.", error);
      setMessage({
        type: "danger",
        text: "데이터를 불러오는 데 실패했습니다.",
      });
    } finally {
      setTableLoading(false);
    }
  }, []);

  // 컴포넌트가 처음 마운트될 때 담당자 목록을 불러옵니다.
  useEffect(() => {
    fetchManagers();
  }, [fetchManagers]);

  // 전체 선택/해제 핸들러
  const handleSelectAll = (e) => {
    if (e.target.checked) {
      // 모든 담당자의 ID를 선택 목록에 추가
      const allManagerIds = managers.map((manager) => manager.managerId);
      setSelectedManagers(allManagerIds);
    } else {
      // 선택 목록을 비움
      setSelectedManagers([]);
    }
  };

  const handleSelectManager = (managerId) => {
    if (selectedManagers.includes(managerId)) {
      // 이미 선택된 항목이면 목록에서 제거
      setSelectedManagers((prevSelected) =>
        prevSelected.filter((id) => id !== managerId)
      );
    } else {
      // 선택되지 않은 항목이면 목록에 추가
      setSelectedManagers((prevSelected) => [...prevSelected, managerId]);
    }
  };
  const handleDeleteSelected = async () => {
    if (selectedManagers.length === 0) {
      alert("삭제할 담당자를 선택해주세요.");
      return;
    }

    if (
      window.confirm(
        `선택된 ${selectedManagers.length}명의 담당자를 정말 삭제하시겠습니까?`
      )
    ) {
      try {
        // 백엔드에 삭제 API(POST /api/managers/delete) 요청
        await axios.delete("/api/managers", {
          params: {
            ids: selectedManagers
          },
          // 2. paramsSerializer 옵션을 추가합니다.
          paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'comma' })
          }
        });

        alert("선택된 담당자가 삭제되었습니다.");
        fetchManagers(); // 목록 새로고침
        setSelectedManagers([]); // 선택 상태 초기화
      } catch (error) {
        console.error("담당자 삭제 중 오류 발생:", error);
        alert("삭제 중 오류가 발생했습니다.");
      }
    }
  };

  // 폼 입력값 변경 핸들러
  const handleNewFormChange = (e) => {
    const { name, value } = e.target;
    setNewFormData((prevState) => ({ ...prevState, [name]: value }));
  };

  const handleEditFormChange = (e) => {
    const { name, value } = e.target;
    setEditFormData((prevState) => ({ ...prevState, [name]: value }));
  };

  // 폼 제출(담당자 등록) 핸들러
  const handleNewFormSubmit = async (e) => {
    e.preventDefault();
    if (!newFormData.managerId || !newFormData.managerName) {
      alert("담당자 ID와 이름은 필수입니다.");
      return;
    }

    setFormLoading(true);
    setMessage({ type: "", text: "" });

    try {
      const response = await axios.post("/api/managers", newFormData);
      if (response.status === 201 || response.status===200) {
        alert("담당자가 성공적으로 등록되었습니다.");
        // 성공 시, 목록을 새로고침하고 폼을 초기화합니다.
        fetchManagers();
        setNewFormData({
          managerId: "",
          managerName: "",
          email: "",
          phoneNumber: "",
        });

        // Bootstrap 모달을 수동으로 닫습니다.
        const modalElement = document.getElementById("managerFormModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message || "담당자 등록 중 오류가 발생했습니다.";
      alert(errorMsg);
    } finally {
      setFormLoading(false);
    }
  };

  const handleEditFormSubmit = async (e) => {
    e.preventDefault();
    if (!editFormData.managerId || !editFormData.managerName) {
      alert("담당자 ID와 이름은 필수입니다.");
      return;
    }

    setFormLoading(true);
    setMessage({ type: "", text: "" });

    try {
      const response = await axios.put(
        "/api/managers/" + editFormData.managerId,
        editFormData
      );
      if (response.status === 200) {
        alert("담당자 정보 수정이 성공적으로 등록되었습니다.");
        // 성공 시, 목록을 새로고침하고 폼을 초기화합니다.
        fetchManagers();
        setEditFormData({
          managerId: "",
          managerName: "",
          email: "",
          phoneNumber: "",
        });

        // Bootstrap 모달을 수동으로 닫습니다.
        const modalElement = document.getElementById("managerEditModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message ||
        "담당자 정보 수정 중 오류가 발생했습니다.";
      alert(errorMsg);
    } finally {
      setFormLoading(false);
    }
  };

  const handleEditClick = (managerToEdit) => {
    setEditFormData({
      managerId: managerToEdit.managerId || "",
      managerName: managerToEdit.managerName || "",
      email: managerToEdit.email || "",
      phoneNumber: managerToEdit.phoneNumber || "",
    });
  };

  return (
    <>
      <header className="mb-3">
        <h3>담당자등록 리스트</h3>
      </header>
      <div className="table-responsive table-container-scrollable">
        <table className="table table-hover align-middle">
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  // `onChange`가 발생하면 `handleSelectAll` 함수를 호출합니다.
                  onChange={handleSelectAll}
                  // 전체 담당자 수와 선택된 담당자 수가 같을 때만 체크됩니다.
                  checked={
                    managers.length > 0 &&
                    selectedManagers.length === managers.length
                  }
                />
              </th>
              <th>담당자 코드</th>
              <th>이름</th>
              <th>연락처</th>
              <th>email</th>
            </tr>
          </thead>
          <tbody>
            {managers.map((manager) => (
              <tr key={manager.managerId}>
                <td>
                  <input
                    type="checkbox"
                    // `checked` 속성은 React의 `selectedManagers` state에 의해 결정됩니다.
                    checked={selectedManagers.includes(manager.managerId)}
                    // `onChange` 이벤트가 발생하면, `handleSelectManager` 함수를 호출하여 state를 업데이트합니다.
                    onChange={() => handleSelectManager(manager.managerId)}
                  />
                </td>
                <td>{manager.managerId}</td>
                <td>
                  <a
                    onClick={() => handleEditClick(manager)}
                    href="#"
                    data-bs-toggle="modal"
                    data-bs-target="#managerEditModal"
                  >
                    {manager.managerName}
                  </a>
                </td>
                <td>{manager.phoneNumber}</td>
                <td>{manager.email}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <footer className="mt-3">
        <button
          className="btn btn-primary mx-3"
          data-bs-toggle="modal"
          data-bs-target="#managerFormModal"
        >
          신규 담당자 등록
        </button>
        <button className="btn btn-primary me-3" onClick={handleDeleteSelected}>
          삭제
        </button>
      </footer>
      <div
        className="modal fade"
        id="managerFormModal"
        tabIndex="-1"
        aria-labelledby="managerFormModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleNewFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="managerFormModalLabel">
                  신규 담당자 등록
                </h1>
                <button
                  type="button"
                  className="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label htmlFor="managerId" className="form-label">
                    담당자 ID <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="managerId"
                    name="managerId"
                    value={newFormData.managerId}
                    onChange={handleNewFormChange}
                    required
                    readOnly
                  />
                </div>
                <div className="mb-3">
                  <label htmlFor="managerName" className="form-label">
                    담당자 이름 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="managerName"
                    name="managerName"
                    value={newFormData.managerName}
                    onChange={handleNewFormChange}
                    required
                  />
                </div>
                <div className="mb-3">
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
                  />
                </div>
                <div className="mb-3">
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
                  />
                </div>
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
                  disabled={formLoading}
                >
                  {formLoading ? "저장 중..." : "저장하기"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div
        className="modal fade"
        id="managerEditModal"
        tabIndex="-1"
        aria-labelledby="managerEditModal"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleEditFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="managerEditModal">
                  담당자 정보 수정
                </h1>
                <button
                  type="button"
                  className="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label htmlFor="managerId" className="form-label">
                    담당자 ID <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="managerId"
                    name="managerId"
                    value={editFormData.managerId}
                    onChange={handleEditFormChange}
                    required
                    readOnly
                  />
                </div>
                <div className="mb-3">
                  <label htmlFor="managerName" className="form-label">
                    담당자 이름 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="managerName"
                    name="managerName"
                    value={editFormData.managerName}
                    onChange={handleEditFormChange}
                    required
                  />
                </div>
                <div className="mb-3">
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
                  />
                </div>
                <div className="mb-3">
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
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  data-bs-dismiss="modal"
                  onClick={() =>
                    setEditFormData({
                      managerId: "",
                      managerName: "",
                      email: "",
                      phoneNumber: "",
                    })
                  }
                >
                  닫기
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={formLoading}
                >
                  {formLoading ? "저장 중..." : "저장하기"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default ManagerPage;
