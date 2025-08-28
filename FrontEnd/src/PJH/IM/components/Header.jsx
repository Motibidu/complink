import { useAuth } from "../contexts/AuthContext";
const Header = () => {
  const { isLoggedIn, logout } = useAuth();
  const handleLogout = (e) => {
    e.preventDefault(); // a 태그의 기본 동작 방지
    logout();
    // 필요하다면 홈페이지로 리디렉션
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
    </>
  );
};

export default Header;
