import React, { useState, useEffect, useCallback } from "react";
import axios from "axios"; // 데이터 전송을 위해 axios 사용
import qs from "qs";

const RegisterItemPage = () => {
  // 폼 데이터를 한 번에 관리하기 위한 state
  const [items, setItems] = useState([]);
  const [selectedItems, setSelectedItems] = useState([]);
  const [message, setMessage] = useState({ type: "", text: "" });
  const [newFormData, setNewFormData] = useState({
    itemName: "인텔 코어i5-14세대 14600K (랩터레이크 리프레시) (정품)",
    category: "CPU",
    purchasePrice: "264480",
    sellingPrice: "296480",
  });
  const [editFormData, setEditFormData] = useState({
    itemId: "",
    itemName: "",
    category: "",
    purchasePrice: "",
    sellingPrice: "",
  });
  const [tableLoading, setTableLoading] = useState(true);
  const [formLoading, setFormLoading] = useState(false);
  const [loading, setLoading] = useState(false);

  // post요청이 완료된 후 다시 fetchItems를 호출해주기 위해 useEffect 바깥에 함수를 정의
  const fetchItems = useCallback(async () => {
    setTableLoading(true);
    try {
      const response = await axios.get("/api/items");
      console.log("response: ", response);
      console.log("response.data: ", response.data);

      const itemsData = Array.isArray(response.data) ? response.data : [];
      setItems(itemsData);
    } catch (error) {
      console.error("품목 목록을 불러오는 데 실패했습니다.", error);
    } finally {
      setTableLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchItems();
  }, [fetchItems]);

  //handle select
  const handleSelectAll = (e) => {
    if (e.target.checked) {
      const allItemIds = items.map((item) => item.itemId);
      setSelectedItems(allItemIds);
    } else {
      setSelectedItems([]);
    }
  };

  const handleSelectItem = (itemId) => {
    if (selectedItems.includes(itemId)) {
      setSelectedItems((prevSelected) =>
        prevSelected.filter((id) => id !== itemId)
      );
    } else {
      setSelectedItems((prevSelected) => [...prevSelected, itemId]);
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedItems.length === 0) {
      alert("삭제할 품목을 선택해주세요.");
      return;
    }

    if (
      window.confirm(
        `선택된 ${selectedItems.length}개의 품목을 정말 삭제하시겠습니까?`
      )
    ) {
      try {
        await axios.delete("/api/items", {
          params: {
            ids: selectedItems,
          },
          paramsSerializer: (params) => {
            return qs.stringify(params, { arrayFormat: "comma" });
          },
        });

        alert("선택된 품목들이 삭제되었습니다.");
        fetchItems(); // 목록 새로고침
        setSelectedItems([]); // 선택 상태 초기화
      } catch (error) {
        console.error("품목 삭제 중 오류 발생:", error);
        alert("삭제 중 오류가 발생했습니다.");
      }
    }
  };

  // handleChange
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

  // submit
  const handleNewFormSubmit = async (e) => {
    e.preventDefault();

    if (
      !newFormData.itemName ||
      !newFormData.purchasePrice ||
      !newFormData.sellingPrice
    ) {
      setMessage({ type: "danger", text: "필수 항목(*)을 모두 입력해주세요." });
      return;
    }

    setLoading(true);
    setMessage({ type: "", text: "" });

    try {
      const response = await axios.post("/api/items", {
        ...newFormData,
        purchasePrice: Number(newFormData.purchasePrice),
        sellingPrice: Number(newFormData.sellingPrice),
      });

      if (response.status === 201) {
        setNewFormData({
          itemName: "",
          category: "",
          purchasePrice: "",
          sellingPrice: "",
        });

        fetchItems();
        alert("품목 등록이 성공적으로 완료되었습니다.");
        const modalElement = document.getElementById("newFormModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
      }
    } catch (error) {
      const errorMsg =
        error.response?.data?.message || "품목 등록 중 오류가 발생했습니다.";
      setMessage({ type: "danger", text: errorMsg });
      console.error("Error submitting form:", error);
    } finally {
      setLoading(false);
    }
  }; // <-- FIX: Added the missing closing brace here

  const handleEditFormSubmit = async (e) => {
    e.preventDefault();

    if (
      !editFormData.itemName ||
      !editFormData.purchasePrice ||
      !editFormData.sellingPrice
    ) {
      setMessage({ type: "danger", text: "필수 항목(*)을 모두 입력해주세요." });
      return;
    }

    setLoading(true);
    setMessage({ type: "", text: "" });

    try {
      const response = await axios.put(
        "/api/items/" + editFormData.itemId, // Corrected URL concatenation
        {
          ...editFormData,
          // BUG FIX: Changed newFormData to editFormData
          purchasePrice: Number(editFormData.purchasePrice),
          sellingPrice: Number(editFormData.sellingPrice),
        }
      );

      if (response.status === 200) {
        setEditFormData({
          itemId: "",
          itemName: "",
          category: "",
          purchasePrice: "",
          sellingPrice: "",
        });
        fetchItems();
        const modalElement = document.getElementById("editFormModal");
        const modalInstance = window.bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
        alert("선택한 품목의 수정이 완료되었습니다.");
      }
    } catch (error) {
      let errorMsg = "품목 수정 중 오류가 발생했습니다.";
      if (error.response?.data) {
        console.error(error);
        // 백엔드가 { fieldName: "error message" } 형태로 보낸 경우
        if (typeof error.response.data === "object") {
          errorMsg = Object.values(error.response.data).join(", ");
        }
        // 백엔드가 문자열로 보낸 경우
        else if (typeof error.response.data === "string") {
          errorMsg = error.response.data;
        }
        setMessage({ type: "danger", text: errorMsg });
      }
    } finally {
      setLoading(false);
    }
  };

  const handleEditClick = (itemToEdit) => {
    setEditFormData({
      itemId: itemToEdit.itemId || "",
      itemName: itemToEdit.itemName || "",
      category: itemToEdit.category || "",
      purchasePrice: itemToEdit.purchasePrice || "",
      sellingPrice: itemToEdit.sellingPrice || "",
    });
  };

  return (
    <>
      <header className="mb-3">
        <h3>품목등록 리스트</h3>
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
                    items.length > 0 && selectedItems.length === items.length
                  }
                />
              </th>
              <th>품목 코드</th>
              <th>이름</th>
              <th>카테고리</th>
              <th>입고가격</th>
              <th>출고가격</th>
            </tr>
          </thead>
          <tbody>
            {items
              ? items.map((item) => (
                  <tr key={item.itemId}>
                    <td>
                      <input
                        type="checkbox"
                        checked={selectedItems.includes(item.itemId)}
                        onChange={() => handleSelectItem(item.itemId)}
                      />
                    </td>
                    <td>{item.itemId}</td>
                    <td>
                      <a
                        onClick={() => handleEditClick(item)}
                        href="#"
                        data-bs-toggle="modal"
                        data-bs-target="#editFormModal"
                      >
                        {item.itemName}
                      </a>
                    </td>
                    <td>{item.category}</td>
                    <td>{item.purchasePrice}</td>
                    <td>{item.sellingPrice}</td>
                  </tr>
                ))
              : ""}
          </tbody>
        </table>
      </div>
      <footer className="mt-3">
        <button
          className="btn btn-primary mx-3"
          data-bs-toggle="modal"
          data-bs-target="#newFormModal"
        >
          신규 품목 등록
        </button>
        <button className="btn btn-primary me-3" onClick={handleDeleteSelected}>
          삭제
        </button>
      </footer>
      <div
        className="modal fade"
        id="newFormModal"
        tabIndex="-1"
        aria-labelledby="itemFormModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleNewFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="itemFormModalLabel">
                  신규 품목 등록
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
                  <label htmlFor="itemName" className="form-label fw-bold">
                    품목명 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="itemName"
                    name="itemName"
                    value={newFormData.itemName}
                    onChange={handleNewFormChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="category" className="form-label fw-bold">
                    카테고리
                  </label>
                  <select
                    className="form-select"
                    id="category"
                    name="category"
                    value={newFormData.category}
                    onChange={handleNewFormChange}
                  >
                    <option value="">카테고리 선택...</option>
                    <option value="CPU">CPU</option>
                    <option value="메인보드">메인보드</option>
                    <option value="그래픽카드">그래픽카드</option>
                    <option value="RAM">RAM</option>
                    <option value="SSD">SSD</option>
                    <option value="케이스/파워">케이스/파워</option>
                  </select>
                </div>

                <div className="mb-3">
                  <label htmlFor="purchasePrice" className="form-label fw-bold">
                    입고 단가 (원) <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    id="purchasePrice"
                    name="purchasePrice"
                    value={newFormData.purchasePrice}
                    onChange={handleNewFormChange}
                    min="0"
                    required
                    placeholder="숫자만 입력"
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="sellingPrice" className="form-label fw-bold">
                    출고 단가 (원) <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    id="sellingPrice"
                    name="sellingPrice"
                    value={newFormData.sellingPrice}
                    onChange={handleNewFormChange}
                    min="0"
                    required
                    placeholder="숫자만 입력"
                  />
                </div>
              </div>

              {message.text && (
                <div
                  className={`alert alert-${message.type} mt-4 mb-0`}
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
        aria-labelledby="itemFormModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <form onSubmit={handleEditFormSubmit}>
              <div className="modal-header">
                <h1 className="modal-title fs-5" id="itemFormModalLabel">
                  품목 수정
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
                  <label htmlFor="itemName" className="form-label fw-bold">
                    품목명 <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="itemName"
                    name="itemName"
                    value={editFormData.itemName}
                    onChange={handleEditFormChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="category" className="form-label fw-bold">
                    카테고리
                  </label>
                  <select
                    className="form-select"
                    id="category"
                    name="category"
                    value={editFormData.category}
                    onChange={handleEditFormChange}
                  >
                    <option value="">카테고리 선택...</option>
                    <option value="CPU">CPU</option>
                    <option value="메인보드">메인보드</option>
                    <option value="그래픽카드">그래픽카드</option>
                    <option value="RAM">RAM</option>
                    <option value="SSD">SSD</option>
                    <option value="케이스/파워">케이스/파워</option>
                  </select>
                </div>

                <div className="mb-3">
                  <label htmlFor="purchasePrice" className="form-label fw-bold">
                    입고 단가 (원) <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    id="purchasePrice"
                    name="purchasePrice"
                    value={editFormData.purchasePrice}
                    onChange={handleEditFormChange}
                    min="0"
                    required
                    placeholder="숫자만 입력"
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="sellingPrice" className="form-label fw-bold">
                    출고 단가 (원) <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    id="sellingPrice"
                    name="sellingPrice"
                    value={editFormData.sellingPrice}
                    onChange={handleEditFormChange}
                    min="0"
                    required
                    placeholder="숫자만 입력"
                  />
                </div>
              </div>
              {message.text && (
                <div
                  className={`alert alert-${message.type} mt-4 mb-0`}
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

export default RegisterItemPage;
