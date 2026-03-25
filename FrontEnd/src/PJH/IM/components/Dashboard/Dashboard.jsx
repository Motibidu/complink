import React, { useState, useEffect } from 'react';
import axios from 'axios';

// 1. React-Bootstrap에서 필요한 컴포넌트들을 import 합니다.
import { Container, Row, Col, Card, Spinner, Alert, Table, Badge } from 'react-bootstrap';

/**
 * [Bootstrap 적용]
 * 오늘의 총 매출액과 신규 주문 건수를 보여주는 대시보드 컴포넌트
 */
const Dashboard = () => {
    // API 데이터 상태 (이전과 동일)
    const [summary, setSummary] = useState({
        totalSellsToday: 0,
        newOrdersToday: 0,
        pendingPaymentCount: 0,
        activeWorkloadCount: 0,
        last7DaysSales: [],
        categoryStockSummary: [],
        topCustomers: [],
        topItemSales: []
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

            <h2 className="mb-4 mt-5">처리할 작업 (To-Do)</h2>
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

            {/* 최근 7일간 매출 통계 */}
            <h2 className="mb-4 mt-5">📊 최근 7일 매출 추이</h2>
            <Card className="shadow-sm mb-4">
                <Card.Body>
                    <Table striped hover responsive>
                        <thead>
                            <tr>
                                <th>날짜</th>
                                <th>주문 건수</th>
                                <th>매출액</th>
                            </tr>
                        </thead>
                        <tbody>
                            {summary.last7DaysSales && summary.last7DaysSales.length > 0 ? (
                                summary.last7DaysSales.map((day, index) => (
                                    <tr key={index}>
                                        <td>{day.date}</td>
                                        <td><Badge bg="primary">{day.salesCount} 건</Badge></td>
                                        <td className="fw-bold">{formatCurrency(day.totalSales)}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="3" className="text-center text-muted">데이터가 없습니다.</td>
                                </tr>
                            )}
                        </tbody>
                    </Table>
                </Card.Body>
            </Card>

            {/* 카테고리별 재고 현황 */}
            <h2 className="mb-4 mt-5">📦 카테고리별 재고 현황</h2>
            <Card className="shadow-sm mb-4">
                <Card.Body>
                    <Table striped hover responsive>
                        <thead>
                            <tr>
                                <th>카테고리</th>
                                <th>품목 수</th>
                                <th>총 재고</th>
                                <th>가용 재고</th>
                                <th>재고 가치</th>
                            </tr>
                        </thead>
                        <tbody>
                            {summary.categoryStockSummary && summary.categoryStockSummary.length > 0 ? (
                                summary.categoryStockSummary.map((category, index) => (
                                    <tr key={index}>
                                        <td><Badge bg="secondary">{category.category}</Badge></td>
                                        <td>{category.totalItems} 개</td>
                                        <td>{category.totalStock.toLocaleString()} 개</td>
                                        <td>{category.totalAvailable.toLocaleString()} 개</td>
                                        <td className="fw-bold">{formatCurrency(category.totalValue)}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="5" className="text-center text-muted">데이터가 없습니다.</td>
                                </tr>
                            )}
                        </tbody>
                    </Table>
                </Card.Body>
            </Card>

            {/* TOP 고객 및 TOP 품목 */}
            <h2 className="mb-4 mt-5">🏆 판매 통계</h2>
            <Row xs={1} lg={2} className="g-4">
                {/* TOP 고객 */}
                <Col>
                    <Card className="shadow-sm h-100">
                        <Card.Header className="bg-primary text-white">
                            <h5 className="mb-0">🥇 TOP 5 고객</h5>
                        </Card.Header>
                        <Card.Body>
                            <Table striped hover size="sm">
                                <thead>
                                    <tr>
                                        <th>고객명</th>
                                        <th>주문 건수</th>
                                        <th>총 구매액</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {summary.topCustomers && summary.topCustomers.length > 0 ? (
                                        summary.topCustomers.map((customer, index) => (
                                            <tr key={index}>
                                                <td>
                                                    {index === 0 && '🥇 '}
                                                    {index === 1 && '🥈 '}
                                                    {index === 2 && '🥉 '}
                                                    {customer.customerName}
                                                </td>
                                                <td>{customer.orderCount} 건</td>
                                                <td className="fw-bold">{formatCurrency(customer.totalPurchase)}</td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan="3" className="text-center text-muted">데이터가 없습니다.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </Table>
                        </Card.Body>
                    </Card>
                </Col>

                {/* TOP 품목 */}
                <Col>
                    <Card className="shadow-sm h-100">
                        <Card.Header className="bg-success text-white">
                            <h5 className="mb-0">🏆 TOP 10 인기 품목</h5>
                        </Card.Header>
                        <Card.Body>
                            <Table striped hover size="sm">
                                <thead>
                                    <tr>
                                        <th>품목명</th>
                                        <th>판매량</th>
                                        <th>매출액</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {summary.topItemSales && summary.topItemSales.length > 0 ? (
                                        summary.topItemSales.map((item, index) => (
                                            <tr key={index}>
                                                <td>
                                                    {index === 0 && '🥇 '}
                                                    {index === 1 && '🥈 '}
                                                    {index === 2 && '🥉 '}
                                                    {item.itemName}
                                                </td>
                                                <td>{item.quantitySold} 개</td>
                                                <td className="fw-bold">{formatCurrency(item.totalRevenue)}</td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan="3" className="text-center text-muted">데이터가 없습니다.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </Table>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default Dashboard;