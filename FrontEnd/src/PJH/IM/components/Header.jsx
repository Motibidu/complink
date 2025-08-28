import { IoPerson } from "react-icons/io5";
import { useAuth } from "../contexts/AuthContext";
import { useState } from "react";
import axios from "axios"; // axios 사용을 권장합니다 (fetch보다 편리)

const Header = () => {
  const { isLoggedIn, logout } = useAuth();
  const [billingKey, setBillingKey] = useState(null);
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

  const PORTONE_API_SECRET =
    "FkLCYZzsKhVsoZxz8aZEWXTiRsRYisWO9CBuzCUuooCjBU78TCMCEmdt3NydMvlG63zysLVjQMLAsdA1";
  const CHANNEL_KEY = "channel-key-d425a2bd-5e2c-4bf8-84c6-9c4713b7c6bb";
  const STORE_ID = "store-95cd6283-735d-4eae-9128-9b804b0b5048";

  // axios 인스턴스 설정 (App.js에서 사용한 axiosInstance가 있다면 그것을 가져와도 좋습니다)
  const api = axios.create({
    baseURL: "/api", // 백엔드 API 기본 경로
    withCredentials: true, // 세션 쿠키 전송을 위해 필수
  });

  // useEffect(() => {
  //   async function issueBillingKey() {
  //     try {
  //       const issueResponse = await axios({
  //         url: "https://api.portone.io/billing-keys",
  //         method: "post",
  //         headers: { Authorization: `PortOne ${PORTONE_API_SECRET}` },
  //         data: {
  //           channelKey: CHANNEL_KEY, // 콘솔 결제 연동 화면에서 채널 연동 시 생성된 채널 키를 입력해주세요.
  //           customer: {
  //             id: "jack981109",
  //             name: {
  //               full: "박지훈",
  //             },
  //             phoneNumber: "010-6230-1825",
  //             email: "jack981109@naver.com", // 고객사에서 관리하는 고객 고유번호
  //           },
  //           method: {
  //             card: {
  //               credential: {
  //                 number: "5376990016734664",
  //                 expiryMonth: "08",
  //                 expiryYear: "29",
  //                 birthOrBusinessRegistrationNumber: "981109",
  //                 passwordTwoDigits: "00",
  //               },
  //             },
  //           },
  //         },
  //       });
  //       // console.log(
  //       //   "issueResponse.data.billingKeyInfo.billingKey: ",
  //       //   issueResponse.data.billingKeyInfo.billingKey
  //       // );
  //       setBillingKey(issueResponse.data.billingKeyInfo.billingKey); // Access billingKey from response data
  //     } catch (error) {
  //       console.error(error);
  //     }
  //   }

  //   // Call the async function when the component mounts
  //   issueBillingKey();
  // }, []);

  async function requestPayment() {
    const PAYMENT_ID_HERE = randomId();
    //console.log('billingKey: ', billingKey);
    // try {
    //   const issueResponse = await axios({
    //     url: `https://api.portone.io/payments/${PAYMENT_ID_HERE}/schedule`,
    //     method: "post",
    //     headers: { Authorization: `PortOne ${PORTONE_API_SECRET}` },
    //     data: {
    //       payment: {
    //         billingKey: billingKey, // 빌링키 발급 API를 통해 발급받은 빌링키
    //         orderName: "월간 이용권 정기결제",
    //         customer: {
    //           id: "customer-1234",
    //           phoneNumber: "010-6230-1825",
    //           email: "jack981109@naver.com",
    //           name: {
    //             full: "ParkJiHoon",
    //           }, // 고객사에서 관리하는 고객 고유번호
    //         },
    //         amount: {
    //           total: 1000,
    //         },
    //         currency: "KRW",
    //       },
    //       timeToPay: "2025-08-30T08:55:00.000Z", // 결제를 시도할 시각
    //     },
    //   });
    //   console.log("issueResponse: ", issueResponse);
    // } catch (e) {
    //   console.error(e);
    // }
    try {
      // --- 항상 단건 결제를 먼저 요청 ---
      const response = await window.PortOne.requestPayment({
        storeId: STORE_ID, // 고객사 storeId
        channelKey: CHANNEL_KEY, // 채널 키
        paymentId: `payment-${randomId()}`,
        orderName: "PCGEAR 단건결제",
        totalAmount: 1000,
        currency: "KRW",
        payMethod: "CARD",
        customer: {
          fullName: "박지훈",
          email: "jack981109@naver.com",
          phoneNumber: "010-6230-1825",
        },
      });

      if (response.code) {
        return alert(`결제 오류: ${response.message}`);
      }

      console.log(response);

      // --- 3. 결제 성공 후, 자체 백엔드에 완료 처리 요청 ---
      //await processPaymentOnServer(response.paymentId);
    } catch (error) {
      console.error("결제 처리 중 오류:", error);
      alert("결제 중 오류가 발생했습니다.");
    }
  }

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
    </>
  );
};

export default Header;
