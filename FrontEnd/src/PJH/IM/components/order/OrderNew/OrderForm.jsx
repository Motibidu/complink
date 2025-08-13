import React, { useState } from "react";
import OrderHeader from "./OrderHeader";
import OrderItems from "./OrderItems";
import NotificationComponent from "../../NotificationComponent";
import "./OrderForm.css";

const OrderForm = () => {
  const [orderHeader, setOrderHeader] = useState({
    orderDate: new Date().toISOString().slice(0, 10),
    managerName: "강감찬",
    managerId: "01A",
    customerName: "미래전자",
    customerId: "0001A",
    deliveryDate: new Date().toISOString().slice(0, 10),
  });

  const [orderItems, setOrderItems] = useState([
    {
      itemName: "iptime 유선랜",
      quantity: 12,
      unitPrice: 35000,
      totalPrice: 420000,
    },
  ]);

  // 요청 상태 관리를 위한 state 추가
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleHeaderChange = (e) => {
    const { name, value } = e.target;
    setOrderHeader((prevHeader) => ({
      ...prevHeader,
      [name]: value,
    }));
  };

  const handleItemsChange = (index, e) => {
    const { name, value } = e.target;
    const newItems = [...orderItems];
    newItems[index][name] = value;

    if (name === "quantity" || name === "unitPrice") {
      const quantity = parseFloat(newItems[index].quantity) || 0;
      const unitPrice = parseFloat(newItems[index].unitPrice) || 0;
      newItems[index].totalPrice = quantity * unitPrice;
    }

    setOrderItems(newItems);
  };

  const handleAddItem = () => {
    setOrderItems([
      ...orderItems,
      { itemName: "", quantity: 1, unitPrice: 0, totalPrice: 0 },
    ]);
  };

  const handleRemoveItem = (index) => {
    const newItems = orderItems.filter((_, i) => i !== index);
    setOrderItems(newItems);
  };

  const totalAmount = orderItems.reduce(
    (acc, item) => acc + item.totalPrice,
    0
  );
  const vatAmount = totalAmount * 0.1;
  const grandAmount = totalAmount + vatAmount;

  // --- 백엔드 요청을 보내도록 handleSubmit 함수 수정 ---
  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);

    // 1. 백엔드 DTO 형식에 맞게 데이터 가공
    const payload = {
      orderDate: orderHeader.orderDate,
      customerName: orderHeader.customerName,
      customerId: orderHeader.customerId,
      managerName: orderHeader.managerName,
      managerId: orderHeader.managerId,
      deliveryDate: orderHeader.deliveryDate,
      status: "접수", // 초기 상태를 '접수'로 지정
      items: orderItems.map((item) => ({
        itemName: item.itemName,
        quantity: parseInt(item.quantity, 10),
        unitPrice: parseFloat(item.unitPrice),
        totalPrice: item.totalPrice, // total -> itemTotal
      })),
      totalAmount: totalAmount,
      vatAmount: vatAmount, // vat -> vatAmount
      grandAmount: grandAmount,
    };

    // ID 필드가 비어있거나 숫자가 아닌 경우 처리
    if (!payload.customerName || !payload.managerName) {
      console.log(payload.customerName);
      console.log(payload.managerName);
      setError("고객사와 담당자 ID를 올바르게 입력해주세요.");
      setIsSubmitting(false);
      return;
    }

    // 2. fetch API를 사용하여 POST 요청 보내기
    try {
      const response = await fetch("/api/order/new", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        // 서버에서 에러 응답이 온 경우
        const errorText = await response.text();
        throw new Error(errorText || "서버에서 오류가 발생했습니다.");
      }

      // 3. 성공 처리
      console.log("주문서 제출 성공:", payload);
      alert("주문서가 성공적으로 제출되었습니다.");
      // TODO: 성공 후 폼 초기화 또는 페이지 이동 로직 추가
    } catch (err) {
      // 4. 실패(에러) 처리
      console.error("주문서 제출 실패:", err);
      setError(err.message);
    } finally {
      // 5. 요청 완료 후 버튼 활성화
      setIsSubmitting(false);
    }
  };

  return (
    <div className="order-form">
      <form className="order-form__form" onSubmit={handleSubmit}>
        <OrderHeader
          orderHeader={orderHeader}
          handleHeaderChange={handleHeaderChange}
        />

        <OrderItems
          orderItems={orderItems}
          handleItemsChange={handleItemsChange}
          handleAddItem={handleAddItem}
          handleRemoveItem={handleRemoveItem}
        />
        <NotificationComponent />
        <div className="order-form__summary">
          {/* ... 총 합계, 부가세, 최종 금액 표시 ... */}
        </div>

        {/* 에러 메시지 표시 영역 */}
        {error && <div className="order-form__error-message">{error}</div>}

        <button
          type="submit"
          className="order-form__button order-form__button--submit"
          disabled={isSubmitting} // 요청 중일 때 버튼 비활성화
        >
          {isSubmitting ? "제출 중..." : "주문서 제출"}
        </button>
      </form>
    </div>
  );
};

export default OrderForm;
