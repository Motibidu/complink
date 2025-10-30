import axios from "axios";
import { useEffect, useState, useCallback } from "react";

function SignupApprove() {
  const [signupReqs, setSignupReq] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchSignupReq = useCallback(async (currentPage) => {
    setIsLoading(true);
    try {
      // 💡 page와 size 파라미터를 포함하여 API 호출
      const resp = await axios.get("/api/users/signup-req", {
        params: {
          page: currentPage,
          size: 10, // 페이지 당 10개씩
        },
      });
      console.log("fetchSignupReq_resp: ", resp.data);

      // 💡 응답 데이터에서 content와 totalPages를 상태에 저장
      setSignupReq(resp.data.content);
      setTotalPages(resp.data.totalPages);
    } catch (err) {
      console.log("회원가입 요청 불러오기 에러:", err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSignupReq(currentPage);
  }, [currentPage, fetchSignupReq]);

  const handleApproval = async (email) => {
    try {
      if (confirm("승인하시겠습니까?")) {
        const resp = await axios.post("/api/users/signup-approve/" + email);
        if (resp.status === 200) {
          alert("회원가입 승인이 완료되었습니다.");
        }

        fetchSignupReq(currentPage);
      }
    } catch (err) {
      console.error("회원가입 승인 에러:", err);
    }
  };

  const handleReject = async (email) => {
    try {
      if (confirm("회원가입 요청을 삭제하시겠습니까?")) {
        const resp = await axios.delete("/api/users/signup-reject/" + email);
        if (resp.status === 204) {
          alert("회원가입 요청이 삭제되었습니다.");
        }

        fetchSignupReq(currentPage);
      }
    } catch (err) {
      console.error("회원가입 승인 에러:", err);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setCurrentPage(newPage);
    }
  };

  return (
    <div className="container-fluid py-4">
      <h2 className="fs-3 fw-bold text-dark border-bottom pb-3 mb-4">
        회원가입 승인 관리
      </h2>
      <p className="text-muted mb-4">
        새로 가입을 요청한 사용자를 검토하고 승인 또는 거부합니다.
      </p>

      <div className="bg-white rounded-3 shadow-sm p-4">
        <h4 className="mb-4 fw-semibold">
          승인 대기 사용자 목록 ({signupReqs.length}명)
        </h4>

        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead className="table-light border-bottom border-2">
              <tr>
                <th scope="col">ID</th>
                <th scope="col">이름</th>
                <th scope="col">이메일</th>
                <th scope="col">가입일</th>
                <th scope="col" className="text-center">
                  액션
                </th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan="5" className="text-center">
                    로딩 중...
                  </td>
                </tr>
              ) : signupReqs.length > 0 ? (
                signupReqs.map((signupReq) => (
                  <tr key={signupReq.id}>
                    <th scope="row">{signupReq.id}</th>
                    <td>{signupReq.name}</td>
                    <td>{signupReq.email}</td>
                    <td>{new Date(signupReq.requestDate).toLocaleString()}</td>
                    <td className="text-center">
                      <button
                        className="btn btn-sm btn-success me-2 rounded-2 fw-medium"
                        onClick={() => handleApproval(signupReq.email)}
                      >
                        승인
                      </button>
                      <button
                        className="btn btn-sm btn-danger rounded-2 fw-medium"
                        // 거부 로직도 필요하다면 별도 함수 구현
                        onClick={() => handleReject(signupReq.email)}
                      >
                        거부
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5" className="text-center">
                    승인 대기 중인 사용자가 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {totalPages > 0 && (
          <nav className="mt-4">
            <ul className="pagination pagination-sm justify-content-center">
              <li
                className={`page-item ${currentPage === 0 ? "disabled" : ""}`}
              >
                <button
                  className="page-link rounded-2"
                  onClick={() => handlePageChange(currentPage - 1)}
                >
                  이전
                </button>
              </li>

              {/* 페이지 번호 렌더링 */}
              {Array.from({ length: totalPages }, (_, i) => (
                <li
                  key={i}
                  className={`page-item ${currentPage === i ? "active" : ""}`}
                >
                  <button
                    className="page-link rounded-2"
                    onClick={() => handlePageChange(i)}
                  >
                    {i + 1}
                  </button>
                </li>
              ))}

              <li
                className={`page-item ${
                  currentPage === totalPages - 1 ? "disabled" : ""
                }`}
              >
                <button
                  className="page-link rounded-2"
                  onClick={() => handlePageChange(currentPage + 1)}
                >
                  다음
                </button>
              </li>
            </ul>
          </nav>
        )}
      </div>
    </div>
  );
}

export default SignupApprove;
