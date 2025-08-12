/**
 * 주문 상태에 따라 다른 부트스트랩 Badge 색상 객체를 반환합니다.
 * @param {string} status - 주문 상태 (예: '출고완료')
 * @returns {{bg: string, text: string}} 부트스트랩 배경 및 텍스트 색상 클래스
 */
export const getStatusBadgeVariant = (status) => {
  switch (status) {
    case "출고완료":
      // 'text-' 접두사를 추가하여 올바른 클래스 이름을 사용합니다.
      return { bg: "bg-success-subtle", text: "text-success-emphasis" };
    case "출고대기":
      return { bg: "bg-warning-subtle", text: "text-warning-emphasis" };
    case "재고부족":
      return { bg: "bg-danger-subtle", text: "text-danger-emphasis" };
    case "취소":
      return { bg: "bg-secondary-subtle", text: "text-secondary-emphasis" };
    case "접수":
    default:
      return { bg: "bg-primary-subtle", text: "text-primary-emphasis" };
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
