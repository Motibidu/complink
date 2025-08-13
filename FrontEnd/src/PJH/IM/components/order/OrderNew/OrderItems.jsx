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
            <th className="order-items__cell">상품명</th>
            <th className="order-items__cell">수량</th>
            <th className="order-items__cell">단가</th>
            <th className="order-items__cell">공급가액</th>
            <th className="order-items__cell">부가세</th>
            <th className="order-items__cell"></th>
          </tr>
        </thead>
        <tbody className="order-items__body">
          {orderItems.map((item, index) => (
            <tr key={index} className="order-items__row">
              <td className="order-items__cell">
                <input
                  className="order-items__input-string"
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
                  name="quantity"
                  value={Number(item.quantity).toLocaleString()}
                  onChange={(e) => handleItemsChange(index, e)}
                />
              </td>
              <td className="order-items__cell">
                <input
                  className="order-items__input"
                  name="unitPrice"
                  value={item.unitPrice}
                  onChange={(e) => handleItemsChange(index, e)}
                />
              </td>

              <td className="order-items__cell cell-number">
                <span className="order-items__total">
                  {Number(item.totalPrice).toLocaleString()}
                </span>
              </td>
              <td className="order-items__cell cell-number">
                <span className="order-items__input" name="vatPrice">
                  {Math.round(item.totalPrice * 0.1).toLocaleString()}
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
            <td className="cell-number">
              <span className="order-items__total">
                {orderItems
                  .reduce((acc, item) => {
                    return acc + Number(item.quantity);
                  }, 0)
                  .toLocaleString()}
              </span>
            </td>
            <td></td>

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
    </div>
  );
};

export default OrderItems;
