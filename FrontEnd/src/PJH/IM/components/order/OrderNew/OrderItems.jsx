import React from "react";

const OrderItems = ({
  orderItems,
  handleItemsChange,
  handleAddItem,
  handleRemoveItem,
}) => {
  return (
    <div className="order-items">
      <h3 className="order-items__title">상품 목록</h3>
      <table className="order-items__table">
        <thead className="order-items__head">
          <tr className="order-items__row">
            <th className="order-items__cell">품번</th>
            <th className="order-items__cell">상품명</th>
            <th className="order-items__cell">수량</th>
            <th className="order-items__cell">단가</th>
            <th className="order-items__cell">금액</th>
            <th className="order-items__cell"></th>
          </tr>
        </thead>
        <tbody className="order-items__body">
          {orderItems.map((item, index) => (
            <tr key={index} className="order-items__row">
              <td className="order-items__cell">
                <input
                  className="order-items__input"
                  type="text"
                  name="partNumber"
                  value={item.partNumber}
                  onChange={(e) => handleItemsChange(index, e)}
                  placeholder="품번"
                />
              </td>
              <td className="order-items__cell">
                <input
                  className="order-items__input"
                  type="text"
                  name="itemName"
                  value={item.itemName}
                  onChange={(e) => handleItemsChange(index, e)}
                  placeholder="상품명"
                />
              </td>
              <td className="order-items__cell">
                <input
                  className="order-items__input"
                  type="number"
                  name="quantity"
                  value={item.quantity}
                  onChange={(e) => handleItemsChange(index, e)}
                />
              </td>
              <td className="order-items__cell">
                <input
                  className="order-items__input"
                  type="number"
                  name="unitPrice"
                  value={item.unitPrice}
                  onChange={(e) => handleItemsChange(index, e)}
                />
              </td>
              <td className="order-items__cell">
                <span className="order-items__total">
                  {item.total.toLocaleString()}
                </span>
              </td>
              <td className="order-items__cell">
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
        </tbody>
      </table>
      <button
        type="button"
        onClick={handleAddItem}
        className="order-items__button order-items__button--add"
      >
        상품 추가
      </button>
    </div>
  );
};

export default OrderItems;
