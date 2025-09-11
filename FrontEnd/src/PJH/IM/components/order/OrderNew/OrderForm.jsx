import React, { useState } from "react";
import OrderHeader from "./OrderHeader";
import OrderItems from "./OrderItems";
import NotificationComponent from "../../NotificationComponent";
import "./OrderForm.css";

const OrderForm = () => {
  const [orderHeader, setOrderHeader] = useState({
    orderDate: new Date().toISOString().slice(0, 10),
  });

  const [orderItems, setOrderItems] = useState([
    {
      category: "CPU",
      itemName: "인텔 코어 울트라5 시리즈2 245K (애로우레이크) (정품)",
      quantity: 1,
      unitPrice: 387630,
      totalPrice: 387630,
    },
    {
      category: "메인보드",
      itemName: "MSI MAG X870 토마호크 WIFI",
      quantity: 1,
      unitPrice: 465470,
      totalPrice: 465470,
    },
    {
      category: "메모리",
      itemName: "SK하이닉스 DDR5-5600 (16GB)",
      quantity: 1,
      unitPrice: 91460,
      totalPrice: 91460,
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

  const handleItemSelect = (index, selectedItem) => {
    const newItems = [...orderItems];

    // 선택한 품목의 정보로 해당 행의 데이터를 업데이트합니다.
    const updatedRow = {
      ...newItems[index], // 기존 행의 다른 값(예: 수량)은 유지될 수 있도록
      category: selectedItem.category,
      itemName: selectedItem.itemName,
      unitPrice: selectedItem.sellingPrice * newItems[index].quantity, // 출고단가(sellingPrice)를 단가로 설정
      totalPrice: (newItems[index].quantity || 1) * selectedItem.sellingPrice,
    };

    newItems[index] = updatedRow;
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
        category: item.category,
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
        {!isSubmitting ? (
          <OrderHeader
            orderHeader={orderHeader}
            handleHeaderChange={handleHeaderChange}
          />
        ) : (
          ""
        )}

        <OrderItems
          orderItems={orderItems}
          handleItemSelect={handleItemSelect}
          handleItemsChange={handleItemsChange}
          handleAddItem={handleAddItem}
          handleRemoveItem={handleRemoveItem}
        />
        <NotificationComponent />

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
