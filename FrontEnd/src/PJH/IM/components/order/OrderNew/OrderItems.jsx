import axios from "axios";
import { useState, useMemo, useEffect } from "react";
import { IoReorderFourOutline } from "react-icons/io5";

const OrderItems = ({
  orderItems,
  handleItemSelect,
  handleItemsChange,
  handleAddItem,
  handleRemoveItem,
}) => {
  const [activeRowIndex, setActiveRowIndex] = useState(null);
  const [items, setItems] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(false);

  const filteredItems = useMemo(() => {
    if (!items) return [];
    return items.filter(
      (item) =>
        item.itemName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.category.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [items, searchTerm]);

  const fetchItems = async () => {
    setLoading(true);
    try {
      const response = await axios.get("/api/order/findAllItems");
      console.log(response.data);
      setItems(response.data);
    } catch (err) {
      console.error("아이템 목록 로드 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleModalItemSelect = (selectedItem) => {
    if (activeRowIndex !== null) {
      handleItemSelect(activeRowIndex, selectedItem);
    }
  };

  const partCategories = [
    "CPU",
    "메인보드",
    "메모리",
    "그래픽카드",
    "SSD",
    "파워",
    "케이스",
    "쿨러",
    "기타",
  ];
  return (
    <div className="order-items">
      <h3 className="order-items__title">상품 목록</h3>
      <table className="order-items__table">
        <thead className="order-items__head">
          <tr className="order-items__row">
            <th className="order-items__cell col-itemList"></th>
            <th className="order-items__cell col-category">부품 종류</th>
            <th className="order-items__cell col-itemName">상품명</th>
            <th className="order-items__cel col-quantity">수량</th>
            <th className="order-items__cell col-price">단가</th>
            <th className="order-items__cell col_price">공급가액</th>
            <th className="order-items__cell col_price">부가세</th>
            <th className="order-items__cell col-delete"></th>
          </tr>
        </thead>
        <tbody className="order-items__body">
          {orderItems.map((orderItem, index) => (
            <tr key={index} className="order-items__row">
              <td>
                <IoReorderFourOutline
                  className="orderHeader__list"
                  size={25}
                  onClick={() => {
                    fetchItems();
                    setActiveRowIndex(index);
                  }}
                  data-bs-toggle="modal"
                  data-bs-target="#itemListModal"
                />
              </td>
              <td className="order-items__cell">
                <select
                  className="order-items__category"
                  value={orderItem.category}
                  onChange={(e) => handleItemsChange(index, e)}
                >
                  {partCategories.map((partCategory) => (
                    <option key={partCategory} value={partCategory}>
                      {partCategory}
                    </option>
                  ))}
                </select>
              </td>
              <td className="order-items__cell .order-items__cell--item-name">
                <input
                  className="order-items__input-string"
                  type="text"
                  name="itemName"
                  value={orderItem.itemName}
                  onChange={(e) => handleItemsChange(index, e)}
                  placeholder="상품명"
                />
              </td>
              <td className="order-items__cell">
                <input
                  className="order-items__input"
                  name="quantity"
                  value={Number(orderItem.quantity).toLocaleString()}
                  onChange={(e) => handleItemsChange(index, e)}
                />
              </td>
              <td className="order-items__cell">
                <input
                  className="order-items__input"
                  name="unitPrice"
                  value={orderItem.unitPrice}
                  onChange={(e) => handleItemsChange(index, e)}
                />
              </td>

              <td className="order-items__cell cell-number">
                <span className="order-items__total">
                  {Number(orderItem.totalPrice).toLocaleString()}
                </span>
              </td>
              <td className="order-items__cell cell-number">
                <span className="order-items__input" name="vatPrice">
                  {Math.round(orderItem.totalPrice * 0.1).toLocaleString()}
                </span>
              </td>
              <td className="order-items__cell cell-center">
                <button
                  type="button"
                  onClick={() => handleRemoveItem(index)}
                  className="order-items__button order-items__button--remove"
                >
                  삭제
                </button>
              </td>
            </tr>
          ))}
          <tr className="order-items__result">
            <td></td>
            <td></td>
            <td className="cell-number">
              <span className="order-items__total">
                {orderItems
                  .reduce((acc, item) => {
                    return acc + Number(item.quantity);
                  }, 0)
                  .toLocaleString()}
              </span>
            </td>

            <td className="cell-number">
              <span className="order-items__total">
                {orderItems
                  .reduce((acc, item) => {
                    return acc + item.totalPrice;
                  }, 0)
                  .toLocaleString()}
              </span>
            </td>
            <td className="cell-number">
              <span className="order-items__total">
                {Math.round(
                  orderItems.reduce((acc, item) => {
                    return acc + item.totalPrice * 0.1;
                  }, 0)
                ).toLocaleString()}
              </span>
            </td>
            <td className="cell-number">
              <span className="order-items__total">
                {Math.round(
                  orderItems.reduce((acc, item) => acc + item.totalPrice, 0) *
                    1.1
                ).toLocaleString()}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      <button
        type="button"
        onClick={handleAddItem}
        className="order-items__button order-items__button--add"
      >
        상품 추가
      </button>
      <div
        className="modal fade"
        id="itemListModal"
        tabIndex="-1"
        aria-labelledby="itemModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-scrollable">
          {" "}
          {/* 스크롤 가능하도록 클래스 추가 */}
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5" id="itemModalLabel">
                품목 선택
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
                  placeholder="품목 코드 또는 이름으로 검색..."
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
              {!loading && filteredItems.length > 0 && (
                <div className="list-group manager-list">
                  {filteredItems.map((item) => (
                    <button
                      type="button"
                      key={item.itemId}
                      className="list-group-item list-group-item-action"
                      data-bs-dismiss="modal"
                      onClick={() => handleModalItemSelect(item)}
                    >
                      <div className="d-flex w-100 justify-content-between">
                        <h6 className="mb-1 fw-bold">
                          {item.itemId}. {item.itemName}
                        </h6>
                        <small className="text-muted">{item.category}</small>
                      </div>
                      <p className="mb-1 text-muted small">
                        입고단가: {item.purchasePrice}
                      </p>
                      <p className="mb-1 text-muted small">
                        출고단가: {item.sellingPrice}
                      </p>
                    </button>
                  ))}
                </div>
              )}

              {/* 4. 데이터가 없거나, 검색 결과가 없을 때 */}
              {!loading && filteredItems.length === 0 && (
                <div className="text-center text-muted py-5">
                  {items.length === 0
                    ? "등록된 품목이 없습니다."
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
    </div>
  );
};

export default OrderItems;
