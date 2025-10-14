import axios from "axios";
import React, { useState, useEffect, useCallback } from "react";

function InventoryStatus() {
  const [items, setItems] = useState([]);
  const [totalStockAmount, setTotalStockAmount] = useState(0);
  const [totalPriceAmout, setTotalPriceAmout] = useState(0);

  useEffect(() => {
    const fetchItems = async () => {
      try {
        const response = await axios.get("/api/items");

        console.log(response.data);
        setItems(response.data);
        const totalStockAmount = response.data.reduce(
          (acc, item) => acc + item.quantityOnHand,
          0
        );
        const totalPriceAmout = response.data.reduce(
          (acc, item) => acc + item.quantityOnHand * item.purchasePrice,
          0
        );
        setTotalStockAmount(totalStockAmount);
        setTotalPriceAmout(totalPriceAmout);
      } catch (error) {
        console.error("품목 목록을 불러오는 데 실패했습니다.", error);
      }
    };
    fetchItems();
  }, []);
  return (
    <>
      <header className="mb-3">
        <h3>재고현황</h3>
      </header>
      <div className="table-responsive table-container-scrollable">
        <table className="table table-hover align-middle">
          <thead>
            <tr>
              <th>품목 코드</th>
              <th>이름 및 카테고리</th>
              <th>총재고수량</th>
              <th>입고단가</th>
              <th>금액</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.itemId}>
                <td>{item.itemId}</td>
                <td>
                  {item.itemName}({item.category})
                </td>
                <td>{item.quantityOnHand}</td>
                <td>{item.purchasePrice.toLocaleString()}</td>
                <td>
                  {(item.quantityOnHand * item.purchasePrice).toLocaleString()}
                </td>
              </tr>
            ))}
            <tr>
              <td></td>
              <td></td>
              <td>{totalStockAmount.toLocaleString()}</td>
              <td></td>
              <td>{totalPriceAmout.toLocaleString()}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </>
  );
}

export default InventoryStatus;
