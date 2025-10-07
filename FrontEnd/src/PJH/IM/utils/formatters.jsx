/**
 * 주문 상태에 따라 다른 부트스트랩 Badge 색상 객체를 반환합니다.
 * 백엔드 OrderStatus Enum의 description (예: '결제 완료', '상품 준비중')을 기준으로 작동합니다.
 * @param {string} orderStatusDescription - 주문 상태의 description 문자열 (예: '결제 완료', '상품 준비중')
 * @returns {{bg: string, text: string}} 부트스트랩 배경 및 텍스트 색상 클래스
 */
export const getStatusBadgeVariant = (orderStatusDescription) => {
  switch (orderStatusDescription) {
    // 결제 관련 상태
    case "결제 대기":
      return { bg: "bg-info-subtle", text: "text-info-emphasis" };
    case "결제 준비":
      return { bg: "bg-primary-subtle", text: "text-primary-emphasis" };
    case "결제 완료":
      return { bg: "bg-success-subtle", text: "text-success-emphasis" };
    case "결제 실패":
      return { bg: "bg-danger-subtle", text: "text-danger-emphasis" };
    case "환불 요청":
      return { bg: "bg-warning-subtle", text: "text-warning-emphasis" };
    case "환불 완료":
      return { bg: "bg-secondary-subtle", text: "text-secondary-emphasis" };

    // 배송/처리 관련 상태
    case "상품 준비중":
      return { bg: "bg-primary-subtle", text: "text-primary-emphasis" };
    case "배송중":
      return { bg: "bg-info-subtle", text: "text-info-emphasis" };
    case "배송 완료":
      return { bg: "bg-success-subtle", text: "text-success-emphasis" };
    case "구매 확정":
      return { bg: "bg-success-subtle", text: "text-success-emphasis" };

    // 취소/기타 상태
    case "주문 취소":
      return { bg: "bg-secondary-subtle", text: "text-secondary-emphasis" };
    case "알 수 없는 오류":
      return { bg: "bg-danger-subtle", text: "text-danger-emphasis" };

    default:
      // 정의되지 않은 상태에 대한 기본값 또는 오류 처리
      return { bg: "bg-light", text: "text-muted" };
  }
};

/**
 * 숫자를 통화 형식(e.g., 10,000)의 문자열로 변환합니다.
 * @param {number} amount - 포맷할 숫자
 * @returns {string} 포맷된 문자열
 */
export const formatCurrency = (amount) => {
  if (typeof amount !== "number") return amount;
  return new Intl.NumberFormat("ko-KR").format(amount);
};