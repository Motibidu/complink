# 🚀 PCGear
> 쇼핑몰 운영 환경을 가정하여 만든 PC 부품 주문/재고/배송 통합 관리 시스템입니다.<br>
> **Access**: https://pcgear.store                **ID**: guest_user  **PW**: visit@pcgear 

## 개요
- **목적**: 주문 생성부터 배송 완료까지의 라이프 사이클을 상태 기반으로 관리합니다. 결제, 배송 웹훅 연동을 통해 상태 전이를 자동화하고, 동시성 제어, 성능 개선, 데이터 정합성에 중점을 두어 개발했습니다. 
 
## ERD
<img width="1450" height="1180" alt="erd" src="https://github.com/user-attachments/assets/acf8e577-a17c-4800-b74d-9be11a6d9a5a" />

| Table | Description |
| :--- | :--- |
| **top_items_sales** | 대시보드에서 품목 판매량 순위를 별도 관리하여 조회 성능을 개선합니다. |
| **inventory audit** | 실재고/가용재고/예약재고 불일치 시 수정과 함께 전/후 수량을 기록하여 원인을 파악하도록 돕습니다.  |

## 아키텍처
<img width="550" height="700" alt="아키텍처 drawio" src="https://github.com/user-attachments/assets/586fc496-3bb7-4bf2-8e5e-f6b110d15dff" />

## 기술 스택
| Category | Tech Stack |
| :--- | :--- |
| **Backend** | Spring Boot / Spring Security / Spring Data JPA / QueryDSL |
| **Database** | MySQL / Redis(Caching) |
| **FrontEnd** | React | 
| **Infra** | AWS EC2, Docker Compose, Jekkins(CI/CD) | Nginx |









