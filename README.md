# 🖥️ [PCGear]: 컴퓨터 조립 및 주문 관리 서비스

![Java](https://img.shields.io/badge/Java-17-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-green) ![MySQL](https://img.shields.io/badge/MySQL-8.0-orange) ![Redis](https://img.shields.io/badge/Redis-Latest-red)

> **복잡한 주문 프로세스와 조립 현황을 실시간으로 관리하는 백엔드 시스템입니다.** > 대규모 트래픽 상황을 가정하여 **캐싱(Redis)과 인덱싱**을 통해 대시보드 조회 성능을 **약 20배 개선**했습니다.

<br>

## ⚡ 성능 최적화 (Performance Optimization)
> **"단순 기능 구현을 넘어, 데이터베이스 부하를 줄이고 응답 속도를 극한으로 줄이는 데 집중했습니다."**

### 1. 대시보드 조회 성능 93% 개선 (캐싱 전략)
- **문제 상황:** - 대시보드 진입 시 `일매출`, `신규주문`, `입금대기` 등 4개의 집계 쿼리가 동시에 발생.
    - JMeter 부하 테스트(300 Users/sec) 결과 평균 응답 속도 **764ms**로 지연 발생.
- **해결 과정:**
    - 데이터의 실시간성과 DB 부하를 고려하여 **Redis(Global Cache)** 도입.
    - 읽기 요청이 압도적으로 많은 대시보드 특성상 **Look-aside** 전략 사용.
    - 데이터 정합성을 위해 주문 상태 변경(`Update`) 시 `@CacheEvict`로 캐시 갱신.
- **결과:**
    - 평균 응답 속도: `764ms` → **`53ms`** (약 15배 단축)
    - 처리량(TPS): `365/sec` → **`9,106/sec`** (약 25배 증가)

| 구분 | 캐싱 미적용 (Before) | 캐싱 적용 (After) | 개선율 |
|:---:|:---:|:---:|:---:|
| **응답 속도 (Latency)** | 764ms | **53ms** | **93% 감소** |
| **처리량 (Throughput)** | 365.5/sec | **9,106.0/sec** | **2400% 증가** |

<br>

### 2. 최후의 보루: DB 인덱싱을 통한 쿼리 튜닝
- **문제 상황:** 캐시 서버 장애(Cache Miss) 발생 시, 수십만 건의 주문 데이터에서 `Full Table Scan`이 발생할 위험 확인.
- **해결:** 대시보드 쿼리의 핵심 조건인 `주문일자(created_at)`와 `주문상태(order_status)`를 결합한 **복합 인덱스(Composite Index)** 적용.
- **결과:** `Type: ALL` (Full Scan) → `Type: Range` (Index Range Scan)로 실행 계획 개선.

<br>

## 🛠️ 기술 스택 (Tech Stack)
- **Language:** Java 17
- **Framework:** Spring Boot 3.x, Spring Data JPA, QueryDSL
- **Database:** MySQL 8.0, Redis
- **Tool:** JMeter (부하 테스트), Swagger (API 문서), Git/Github

<br>

## 🏛️ 아키텍처 (Architecture)
*(여기에 draw.io 등으로 그린 간단한 구조도 이미지를 넣으면 베스트입니다)*
`[Client] -> [Load Balancer] -> [Spring Boot Server] -> [Redis(Cache)] / [MySQL(DB)]`

<br>

## 📋 핵심 기능 (Features)
- **주문 관리:** 주문 생성, 취소 및 상태 변경 (상태 패턴 적용 고려)
- **대시보드:** 실시간 매출 및 작업 현황 집계 (Redis 캐싱 적용)
- **결제 시스템:** PortOne API 연동 및 검증 로직
- **조립 관리:** 부품별 시리얼 넘버 관리 및 조립 단계 추적

<br>

## 💾 ERD 설계
*(ERD Cloud나 Workbench 캡처 이미지를 넣으세요)*

<br>

## 🔥 트러블 슈팅 (Troubleshooting)
### N+1 문제 해결
- **문제:** 주문 목록 조회(`findAll`) 시 연관된 `Member`, `Delivery` 엔티티를 가져오기 위해 추가 쿼리가 N번 발생하는 문제 확인.
- **해결:** `Fetch Join`을 적용하여 쿼리 발생 횟수를 `1 + N`회에서 **1회**로 단축.

### Null Pointer Exception 방어
- **문제:** `Left Join` 사용 시 매니저가 없는 주문 건에서 `manager.getName()` 호출 시 NPE 발생.
- **해결:** DTO 변환 로직에 `Optional` 처리 및 `Null Check` 로직을 추가하여 안정성 확보.

```java
// 개선된 코드 예시
this.managerName = (order.getManager() != null) ? order.getManager().getName() : "미배정";
