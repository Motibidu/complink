import React from "react";

const EmptyDetails = () => (
  <div className="flex items-center justify-center h-full">
    <div className="text-center">
      <p className="text-xl font-semibold text-gray-700">
        주문을 선택해주세요.
      </p>
      <p className="text-gray-500 mt-2">
        왼쪽 목록에서 주문을 선택하면 상세 내역이 표시됩니다.
      </p>
    </div>
  </div>
);

export default EmptyDetails;
