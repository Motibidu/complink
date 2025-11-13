import { IoPerson } from "react-icons/io5";
import { useEffect, useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import axios from "axios"; // axios 사용을 권장합니다 (fetch보다 편리)

//

const Header = () => {
  const { isLoggedIn, logout } = useAuth();
  const [userRole, setUserRole] = useState("");
  const [userId, setUserId] = useState("");

  // 📌 [수정] useState를 올바르게 호출하도록 수정
  const [webhookMessage, setWebhookMessage] = useState("");

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

  useEffect(() => {
    // 📌 [수정] 로그인 상태일 때만 사용자 정보를 가져오도록 수정
    if (isLoggedIn) {
      const fetchUserRole = async () => {
        try {
          const resp = await axios.get("/api/users/userRole");
          console.log("fetchUserRole_resp: ", resp.data);
          setUserRole(String(resp.data));
        } catch (err) {
          console.log("사용자 역할 확인 에러:", err);
        }
      };

      const fetchUserId = async () => {
        try {
          const resp = await axios.get("/api/users/userId");
          console.log("fetchUserId_resp: ", resp.data);
          setUserId(String(resp.data));
        } catch (err) {
          console.log("사용자 아이디 가져오기:", err);
        }
      };
      fetchUserRole();
      fetchUserId();
    }
  }, [isLoggedIn]); // 📌 [수정] isLoggedIn이 변경될 때마다 실행

  const STORE_ID = import.meta.env.VITE_PORTONE_STORE_ID;
  const TOSSPAY_CHANNEL_KEY = import.meta.env.VITE_PORTONE_TOSSPAY_CHANNEL_KEY;

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
      storeId: STORE_ID,
      channelKey: TOSSPAY_CHANNEL_KEY,
      billingKeyMethod: "EASY_PAY",
      issueId: `issue-${randomId()}`,
      issueName: "PCGear 정기결제", // 📌 [수정] 결제창에 표시될 이름
      customer: {
        customerId: userId,
      },
      noticeUrls: [
        "https://f177722fba12.ngrok-free.app/payment/webhook-verify",
      ],
    });

    if (response.code) {
      return alert(`결제 오류: ${response.message}`);
    }

    console.log(response);

    // --- 3. 결제 성공 후, 자체 백엔드에 완료 처리 요청 ---
    const isServerProcessSuccess = await processPaymentOnServer(
      response.billingKey
    );

    // 📌 [추가] 서버 처리 성공/실패 시 모달 제어
    if (isServerProcessSuccess) {
      if (paymentModal) paymentModal.hide(); // 기존 모달 닫기
      successModal.show(); // 새로운 성공 모달 열기
    } else {
      alert(
        "빌링키 등록은 성공했으나 서버에 구독을 등록하는 중 문제가 발생했습니다. 관리자에게 문의하세요."
      );
      paymentModal.hide();
    }
  }

  const processPaymentOnServer = async (billingKey) => {
    try {
      const response = await api.post("/payment/subscribe", {
        billingKey: billingKey,
        billingKeyMethod: "EASY_PAY",
        amount: 1000,
        // 📌 [추가] 주문(구독) 이름을 백엔드로 전달
        orderName: "PCGear 월간 플랜",
      });
      if (response.status === 200) {
        setWebhookMessage(response.data);
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
          <a className="header__logo" href="/">
            PCGear
          </a>
          <div className="header__controls">
            {userRole === "ADMIN" ? ( // 📌 [수정] == "ADMIN"
              <div className="dropdown- header__admin-link">
                <a
                  href="#"
                  className="dropdown-toggle"
                  data-bs-toggle="dropdown"
                >
                  관리자
                </a>
                <ul className="dropdown-menu dropdown-menu-end">
                  <li>
                    <a className="dropdown-item" href="/admin/signup-approve">
                      회원가입 승인
                    </a>
                  </li>
                </ul>
              </div>
            ) : (
              ""
            )}
          </div>

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
                    <a
                      className="dropdown-item"
                      href="#"
                      data-bs-toggle="modal"
                      data-bs-target="#paymentModal"
                    >
                      정기 결제
                    </a>
                  </li>
                  <li>
                    <hr className="dropdown-divider" />
                  </li>
                </>
              )}

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

      {/* --- [수정] 정기 결제 모달 UI --- */}
      <div
        className="modal fade"
        id="paymentModal"
        tabIndex="-1"
        aria-labelledby="paymentModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          {" "}
          {/* 1. modal-dialog-centered 추가 */}
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5" id="paymentModalLabel">
                정기 결제 등록 {/* 2. 타이틀 변경 */}
              </h1>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>
            <div className="modal-body">
              {/* 3. 결제 플랜 카드 UI 추가 */}
              <div className="card shadow-sm border-primary mb-4">
                <div className="card-body text-center p-4">
                  <h5 className="card-title text-primary">이용 플랜</h5>
                  <h1 className="card-text display-4 fw-bold my-3">
                    월 1,000원
                  </h1>
                  <p className="text-muted">
                    PCGear 관리자 서비스 이용료가 매월 자동으로 청구됩니다.
                  </p>
                  <ul className="list-unstyled text-start mt-3 mb-0">
                    <li>✓ 모든 관리자 기능 무제한 이용</li>
                    <li>✓ 언제든지 해지 가능</li>
                  </ul>
                </div>
              </div>

              <p className="text-muted small">
                '정기 결제 등록하기' 버튼을 누르면 포트원(토스페이) 결제창이
                뜹니다.
                <br />
                빌링키(결제수단) 등록이 완료되면, 매월 1일에 1,000원이 자동
                청구됩니다.
              </p>

              {/* 4. 약관 동의 체크박스 */}
              <div className="form-check mt-3">
                <input
                  className="form-check-input"
                  type="checkbox"
                  value=""
                  id="flexCheckDefault"
                />
                <label className="form-check-label" htmlFor="flexCheckDefault">
                  정기 결제 및 서비스 이용 약관에 동의합니다.
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
                1,000원 정기 결제 등록하기 {/* 5. 버튼 텍스트 변경 */}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* --- (결제 성공 모달은 기존과 동일) --- */}
      <div className="modal fade" id="paymentSuccessModal">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <div className="modal-body text-center py-5">
              <h3 className="mt-3">정기 결제 등록 완료!</h3>
              <p className="text-muted">{webhookMessage}</p>
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
