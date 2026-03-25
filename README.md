# 🚀 PCGear
> **Access**: https://pcgear.store                **ID**: guest_user  **PW**: visit@pcgear 

## 개요
> 주문 생성부터 배송 완료까지의 라이프 사이클을 상태 기반으로 관리합니다. 결제, 배송은 웹훅 연동을 통해 상태 전이를 자동화시켰고, 동시성 제어, 성능 개선, 데이터 정합성에 중점을 두어 개발했습니다. 
 
## ERD
<img width="1450" height="1180" alt="pcgear erd" src="https://github.com/user-attachments/assets/8105a21c-3ccc-4cfb-a1fb-010c5ce8c8c8" />

| Table | Description |
| :--- | :--- |
| **top_items_sales** | 품목 판매량 순위를 별도 관리하여 대시보드 조회 성능을 개선합니다. |
| **inventory audit** | 실재고/가용재고/예약재고 수량 불일치 시 수정과 함께 전/후 수량을 기록하여 원인을 파악하도록 돕습니다.  |

## 아키텍처
<img width="550" height="700" alt="아키텍처 drawio" src="https://github.com/user-attachments/assets/586fc496-3bb7-4bf2-8e5e-f6b110d15dff" />

## 기술 스택
| Category | Tech Stack |
| :--- | :--- |
| **Backend** | Spring Boot / Spring Security / Spring Data JPA / QueryDSL |
| **Database** | MySQL / Redis(Caching) |
| **FrontEnd** | React | 
| **Infra** | AWS EC2, Docker Compose, Jekkins(CI/CD) | Nginx |

- **PortOne**: 결제 링크를 생성합니다. 결제 완료 시 백엔드에서 웹훅을 수신하고, 멱등성 검사 및 금액 위변조를 검증합니다.
- **CoolSMS**: 포트원에서 생성한 결제링크를 고객에게 문자로 전송합니다.
- **Delivery Tracker**: 배송 상태 변동마다 웹훅으로 수신하고 상태를 업데이트 합니다.
- **Server-Sent-Events**: 주문 접수, 결제 완료, 배송 정보 변동 마다 실시간 알림을 발신합니다.









