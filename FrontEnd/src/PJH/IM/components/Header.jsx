import { IoPerson } from "react-icons/io5";
import { useAuth } from "../contexts/AuthContext";
import axios from "axios"; // axios 사용을 권장합니다 (fetch보다 편리)

const Header = () => {
  const { isLoggedIn, logout } = useAuth();
  const handleLogout = (e) => {
    e.preventDefault(); // a 태그의 기본 동작 방지
    logout();
    // 필요하다면 홈페이지로 리디렉션
  };
  function randomId() {
    return [...crypto.getRandomValues(new Uint32Array(2))]
      .map((word) => word.toString(16).padStart(8, "0"))
      .join("");
  }

  const STORE_ID = import.meta.env.VITE_PORTONE_STORE_ID;
  const TOSSPAY_CHANNEL_KEY = import.meta.env.VITE_PORTONE_TOSSPAY_CHANNEL_KEY;

  console.log(`[ENV] STORE_ID: "${STORE_ID}" (Type: ${typeof STORE_ID})`);
  console.log(
    `[ENV] CHANNEL_KEY: "${TOSSPAY_CHANNEL_KEY}" (Type: ${typeof TOSSPAY_CHANNEL_KEY})`
  );

  // axios 인스턴스 설정 (App.js에서 사용한 axiosInstance가 있다면 그것을 가져와도 좋습니다)
  const api = axios.create({
    baseURL: "/api", // 백엔드 API 기본 경로
    withCredentials: true, // 세션 쿠키 전송을 위해 필수
  });

  async function requestPayment() {
    const paymentModalEl = document.getElementById("paymentModal");
    const paymentModal = window.bootstrap.Modal.getInstance(paymentModalEl);
    const successModalEl = document.getElementById("paymentSuccessModal");
    const successModal = new window.bootstrap.Modal(successModalEl);

    console.log(TOSSPAY_CHANNEL_KEY);
    console.log(STORE_ID);

    // 토스페이 빌링키 발급 요청
    const response = await window.PortOne.requestIssueBillingKey({
      storeId: STORE_ID, // 고객사 storeId로 변경해주세요.
      channelKey: TOSSPAY_CHANNEL_KEY, // 콘솔 결제 연동 화면에서 채널 연동 시 생성된 채널 키를 입력해주세요.
      billingKeyMethod: "EASY_PAY",
      issueId: `issue-${randomId()}`,
      issueName: "test-issueName",
      customer: {
        customerId: `customer-${randomId()}`,
      },
      redirectUrl: "http://localhost",
      noticeUrls: ["https://c2216c116dba.ngrok-free.app"],
    });

    if (response.code) {
      return alert(`결제 오류: ${response.message}`);
    }

    console.log(response);

    // --- 3. 결제 성공 후, 자체 백엔드에 완료 처리 요청 ---
    const isServerProcessSuccess = await processPaymentOnServer(
      response.billingKey
    );
    if (isServerProcessSuccess) {
      if (paymentModal) paymentModal.hide(); // 기존 모달 닫기
      successModal.show(); // 새로운 성공 모달 열기
      // 여기서 모달을 닫는 로직을 추가할 수도 있습니다.
    } else {
      // processPaymentOnServer 내부에서 이미 에러 알림/로깅을 했을 수 있음
      alert(
        "결제는 성공했으나 서버에 기록하는 중 문제가 발생했습니다. 관리자에게 문의하세요."
      );
      paymentModal.hide();
    }
  }
  const processPaymentOnServer = async (billingKey) => {
    try {
      const response = await api.post("/payment/subscribe", {
        billingKey: billingKey,
        orderName: "pcgear 정기결제",
        amount: 1000,
      });
      if (response.status === 200) {
        console.log("서버 처리 성공:", response.data);
        return true;
      }
      return false;
    } catch (err) {
      console.error("서버 처리중 오류: ", err);
      return false;
    }
  };

  return (
    <>
      <header className="header">
        <div className="header__container">
          <a className="header__logo">PCGear</a>
          <div className="dropdown">
            <a
              href="#"
              className="dropdown-toggle"
              data-bs-toggle="dropdown"
              aria-expanded="false"
            >
              <IoPerson size={28} />
            </a>

            <ul className="dropdown-menu dropdown-menu-end">
              {/* isLoggedIn이 true일 때만 '마이페이지'와 '설정'을 보여줌 */}
              {isLoggedIn && (
                <>
                  <li>
                    <a className="dropdown-item" href="#">
                      마이페이지
                    </a>
                  </li>
                  <li>
                    <a className="dropdown-item" href="#">
                      설정
                    </a>
                  </li>
                  <li>
                    {/* 1. 모달을 열기 위한 data-bs-* 속성 추가 */}
                    <a
                      className="dropdown-item"
                      href="#"
                      data-bs-toggle="modal"
                      data-bs-target="#paymentModal"
                    >
                      결제하기
                    </a>
                  </li>
                  <li>
                    <hr className="dropdown-divider" />
                  </li>
                </>
              )}

              {/* 로그인 상태에 따라 '로그인' 또는 '로그아웃'을 보여줌 */}
              {isLoggedIn ? (
                <li>
                  <a className="dropdown-item" href="#" onClick={handleLogout}>
                    로그아웃
                  </a>
                </li>
              ) : (
                <li>
                  <a className="dropdown-item" href="/login">
                    로그인
                  </a>
                </li>
              )}
            </ul>
          </div>
        </div>
      </header>
      <div
        className="modal fade"
        id="paymentModal"
        tabIndex="-1"
        aria-labelledby="paymentModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog">
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5" id="paymentModalLabel">
                결제 진행
              </h1>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>
            <div className="modal-body">
              <p>여기에 결제 관련 폼이나 정보를 입력하세요.</p>
              {/* 예: 결제 수단 선택, 약관 동의 등 */}
              <div className="form-check">
                <input
                  className="form-check-input"
                  type="checkbox"
                  value=""
                  id="flexCheckDefault"
                />
                <label className="form-check-label" htmlFor="flexCheckDefault">
                  구매 약관에 동의합니다.
                </label>
              </div>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                취소
              </button>
              <button
                onClick={requestPayment}
                type="button"
                className="btn btn-primary"
              >
                결제하기
              </button>
            </div>
          </div>
        </div>
      </div>
      <div className="modal fade" id="paymentSuccessModal">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <div className="modal-body text-center py-5">
              <h3 className="mt-3">결제 완료!</h3>
              <p className="text-muted">결제가 성공적으로 처리되었습니다.</p>
              <button
                type="button"
                className="btn btn-primary mt-3"
                data-bs-dismiss="modal"
              >
                확인
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Header;

//const INICIS_CHANNEL_KEY = "channel-key-d425a2bd-5e2c-4bf8-84c6-9c4713b7c6bb";
//const TOSSPAYMENTS_CHANNEL_KEY ="channel-key-9ae5df13-cd44-4b88-8605-e1eedb878ff9";
// 토스페이 간편결제
// const response = await window.PortOne.requestPayment({
//   storeId: STORE_ID, // 고객사 storeId로 변경해주세요.
//   channelKey: TOSSPAY_CHANNEL_KEY, // 콘솔 결제 연동 화면에서 채널 연동 시 생성된 채널 키를 입력해주세요.
//   paymentId: `payment${Math.random().toString(36).slice(2)}`,
//   orderName: "나이키 와플 트레이너 2 SD",
//   totalAmount: 1000,
//   currency: "CURRENCY_KRW",
//   payMethod: "EASY_PAY",
// });

// 이니시스 결제요청
// const response = await window.PortOne.requestPayment({
//   storeId: STORE_ID, // 고객사 storeId
//   channelKey: INICIS_CHANNEL_KEY, // 채널 키
//   paymentId: `payment-${randomId()}`,
//   orderName: "PCGEAR 단건결제",
//   totalAmount: 1000,
//   currency: "KRW",
//   payMethod: "EASY_PAY",
//   issueBillingKey: true,
//   customer: {
//     fullName: "박지훈",
//     email: "jack981109@naver.com",
//     phoneNumber: "010-6230-1825",
//   },
//   easyPay: {
//     easyPayProvider: "APPLEPAY",
//   },
// });

// 토스페이먼츠 빌링키 발급
// const response = await window.PortOne.requestIssueBillingKey({
//   storeId: STORE_ID, // 고객사 storeId로 변경해주세요.
//   channelKey: TOSSPAYMENTS_CHANNEL_KEY, // 콘솔 결제 연동 화면에서 채널 연동 시 생성된 채널 키를 입력해주세요.
//   billingKeyMethod: "CARD",
// });
