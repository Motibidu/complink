# PCGear Monitoring System

Prometheus + Grafana 기반 모니터링 시스템

## 📊 구성 요소

- **Prometheus**: 메트릭 수집 및 저장
- **Grafana**: 대시보드 시각화
- **Spring Boot Actuator**: 애플리케이션 메트릭 노출
- **Micrometer**: 메트릭 계측 라이브러리

## 🚀 빠른 시작

### 1. Spring Boot 애플리케이션 실행

```bash
cd BackEnd/pcgear
./gradlew bootRun
```

애플리케이션이 `http://localhost:8080`에서 실행됩니다.

### 2. Prometheus & Grafana 실행

```bash
cd BackEnd/pcgear/monitoring
docker-compose up -d
```

### 3. 접속 정보

| 서비스 | URL | 계정 |
|--------|-----|------|
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin / admin123 |
| Spring Boot Actuator | http://localhost:8080/actuator | - |
| Prometheus Metrics | http://localhost:8080/actuator/prometheus | - |

## 📈 대시보드 확인

1. Grafana 접속: http://localhost:3000
2. 로그인: `admin` / `admin123`
3. 대시보드 확인: `PCGear E-Commerce Monitoring`

자동으로 프로비저닝된 대시보드가 표시됩니다.

## 🔍 모니터링 항목

### 시스템 메트릭
- ✅ Application Status (UP/DOWN)
- ✅ CPU Usage
- ✅ Heap Memory Usage
- ✅ Database Connection Pool
- ✅ Request Rate
- ✅ Response Time (p95, p99)
- ✅ HTTP Status Codes

### 비즈니스 메트릭
- ✅ Order Created/Failed
- ✅ Payment Success/Failed
- ✅ Stock Out Events
- ✅ Inventory Reconciliation Failures
- ✅ Compensation Retry Count

## 🔔 알림 규칙

다음 상황에서 알림이 발생합니다:

| 알림 | 조건 | 심각도 |
|------|------|--------|
| ApplicationDown | 애플리케이션 다운 1분 이상 | Critical |
| HighErrorRate | 에러율 5% 이상 2분 | Warning |
| HighMemoryUsage | 힙 메모리 90% 이상 5분 | Warning |
| HighCpuUsage | CPU 80% 이상 5분 | Warning |
| SlowResponseTime | p95 응답 시간 1초 이상 5분 | Warning |
| DatabaseConnectionFailure | DB 커넥션 0개 1분 | Critical |
| HighPaymentFailureRate | 결제 실패율 10% 이상 5분 | Critical |

## 🛠️ 커스텀 메트릭 사용법

비즈니스 로직에서 메트릭을 기록하려면:

### 주문 생성 예시

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final Counter orderCreatedCounter;
    private final Counter orderFailedCounter;

    public void createOrder(OrderRequest request) {
        try {
            // 주문 생성 로직
            // ...

            orderCreatedCounter.increment();
        } catch (Exception e) {
            orderFailedCounter.increment();
            throw e;
        }
    }
}
```

### 결제 성공/실패 예시

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final Counter paymentSuccessCounter;
    private final Counter paymentFailedCounter;

    public void processPayment(PaymentRequest request) {
        try {
            // 결제 처리
            // ...

            paymentSuccessCounter.increment();
        } catch (Exception e) {
            paymentFailedCounter.increment();
            throw e;
        }
    }
}
```

### 외부 API 호출 시간 측정

```java
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final Timer externalApiTimer;

    public void callDeliveryApi() {
        externalApiTimer.record(() -> {
            // 외부 API 호출
            restClient.post()...
        });
    }
}
```

### 재고 부족 예시

```java
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final Counter stockOutCounter;

    public void reserveStock(Long itemId, int quantity) {
        if (availableStock < quantity) {
            stockOutCounter.increment();
            throw new StockOutException();
        }
    }
}
```

## 📊 Actuator 엔드포인트

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

응답 예시:
```json
{
  "status": "UP",
  "components": {
    "db": "UP",
    "redis": "UP",
    "diskSpace": "UP",
    "custom": {
      "status": "UP",
      "details": {
        "database": "UP",
        "redis": "UP",
        "memoryUsage": "45.2%"
      }
    }
  }
}
```

### Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus Format
```bash
curl http://localhost:8080/actuator/prometheus
```

## 🔧 설정 파일

### Prometheus 설정
- `prometheus/prometheus.yml`: Prometheus 메인 설정
- `prometheus/alerts.yml`: 알림 규칙

### Grafana 설정
- `grafana/provisioning/datasources/prometheus.yml`: 데이터소스 자동 설정
- `grafana/provisioning/dashboards/dashboard.yml`: 대시보드 프로비저닝 설정
- `grafana/dashboards/pcgear-dashboard.json`: PCGear 대시보드 템플릿

## 🐛 트러블슈팅

### Prometheus가 메트릭을 수집하지 못할 때

1. Spring Boot 애플리케이션이 실행 중인지 확인
```bash
curl http://localhost:8080/actuator/prometheus
```

2. Docker 컨테이너가 실행 중인지 확인
```bash
docker-compose ps
```

3. Prometheus 타겟 상태 확인
- http://localhost:9090/targets

### Grafana 대시보드가 표시되지 않을 때

1. 데이터소스 확인
- http://localhost:3000/datasources
- Prometheus 데이터소스가 정상인지 확인

2. 대시보드 수동 import
- Dashboards → Import → `grafana/dashboards/pcgear-dashboard.json` 업로드

### Windows에서 host.docker.internal 작동하지 않을 때

`prometheus/prometheus.yml` 수정:
```yaml
# 변경 전
- targets: ['host.docker.internal:8080']

# 변경 후 (호스트 IP 사용)
- targets: ['192.168.x.x:8080']
```

## 📝 운영 환경 적용 시 주의사항

1. **Actuator 보안 강화**
   - IP 화이트리스트 적용
   - 인증/인가 추가
   ```properties
   management.endpoints.web.exposure.include=health,metrics,prometheus
   management.server.port=9999  # 별도 포트 사용
   ```

2. **Prometheus 데이터 보존 기간**
   ```yaml
   command:
     - '--storage.tsdb.retention.time=30d'  # 30일 보관
   ```

3. **Grafana 알림 설정**
   - Alerting → Contact points
   - 이메일, Slack, Discord 등 연동

4. **리소스 제한**
   ```yaml
   services:
     prometheus:
       deploy:
         resources:
           limits:
             cpus: '1'
             memory: 1G
   ```

## 🔗 참고 자료

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/docs)

## 📞 문의

모니터링 시스템 관련 문의: admin@pcgear.store
