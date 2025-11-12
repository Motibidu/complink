import React, { useState, useEffect } from 'react';
import axios from 'axios';

// 1. React-Bootstrap에서 필요한 컴포넌트들을 import 합니다.
import { Container, Row, Col, Card, Spinner, Alert } from 'react-bootstrap';

/**
 * [Bootstrap 적용]
 * 오늘의 총 매출액과 신규 주문 건수를 보여주는 대시보드 컴포넌트
 */
const Dashboard = () => {
    // API 데이터 상태 (이전과 동일)
    const [summary, setSummary] = useState({
        totalSellsToday: 0,
        newOrdersToday: 0
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // 데이터 페칭 (이전과 동일)
    useEffect(() => {
        const fetchTodaySummary = async () => {
            try {
                setLoading(true);
                // 백엔드 API 호출 (이 API는 Spring Boot에 구현해야 합니다)
                const response = await axios.get('/api/dashboard/today-summary');
                console.log(response.data);
                setSummary(response.data);
                setError(null);
            } catch (err) {
                console.error("대시보드 요약 데이터 로드 실패:", err);
                setError("데이터를 불러오는 데 실패했습니다.");
            } finally {
                setLoading(false);
            }
        };

        fetchTodaySummary();
    }, []);

    // 헬퍼 함수: 숫자 포맷팅 (이전과 동일)
    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('ko-KR', { 
            style: 'currency', 
            currency: 'KRW' 
        }).format(amount);
    };

    // 2. UI 렌더링 (Bootstrap 컴포넌트 사용)

    // 로딩 중일 때: Spinner 컴포넌트 사용
    if (loading) {
        return (
            <Container className="d-flex justify-content-center align-items-center" style={{ height: '200px' }}>
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Loading...</span>
                </Spinner>
            </Container>
        );
    }

    // 에러 발생 시: Alert 컴포넌트 사용
    if (error) {
        return (
            <Container className="py-4">
                <Alert variant="danger">{error}</Alert>
            </Container>
        );
    }

    // 렌더링 (Card, Row, Col 컴포넌트 사용)
    return (
        <Container className="py-4">
            <h2 className="mb-4">오늘의 현황</h2>
            <Row xs={1} md={2} className="g-4">
                {/* 총 매출액 카드 */}
                <Col>
                    <Card className="text-center shadow-sm h-100">
                        <Card.Body>
                            <Card.Title as="h5" className="text-muted">
                                오늘 총 매출액
                            </Card.Title>
                            <Card.Text className="display-4 fw-bold text-success">
                                {formatCurrency(summary.totalSellsToday)}
                            </Card.Text>
                        </Card.Body>
                    </Card>
                </Col>

                {/* 신규 주문 카드 */}
                <Col>
                    <Card className="text-center shadow-sm h-100">
                        <Card.Body>
                            <Card.Title as="h5" className="text-muted">
                                오늘 신규 주문
                            </Card.Title>
                            <Card.Text className="display-4 fw-bold text-primary">
                                {summary.newOrdersToday} 건
                            </Card.Text>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            <h2 className="mb-4">처리할 작업 (To-Do)</h2>
            <Row xs={1} md={2} className="g-4">
                {/* 결제 대기 카드 */}
                <Col>
                    <Card className="text-center shadow-sm h-100 border-warning border-3">
                        <Card.Body>
                            <Card.Title as="h5" className="text-muted">
                                🔔 결제 대기
                            </Card.Title>
                            <Card.Text className="display-4 fw-bold text-warning">
                                {summary.pendingPaymentCount} 건
                            </Card.Text>
                        </Card.Body>
                    </Card>
                </Col>

                {/* 처리할 작업 큐 카드 */}
                <Col>
                    <Card className="text-center shadow-sm h-100 border-info border-3">
                        <Card.Body>
                            <Card.Title as="h5" className="text-muted">
                                🛠️ 작업 큐 (조립/배송)
                            </Card.Title>
                            <Card.Text className="display-4 fw-bold text-info">
                                {summary.activeWorkloadCount} 건
                            </Card.Text>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default Dashboard;