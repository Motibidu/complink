# PC Gear - PC 부품 쇼핑몰 ERP 시스템

PC 부품 주문/재고/배송을 통합 관리하는 웹 기반 ERP 시스템<br>
**Access**: https://pcgear.store  
**ID**: staff  
**PW**: 123  

## 🛠 Tech Stack
**Backend**: Spring Boot, Spring Data JPA, QueryDSL, MySQL  
**Frontend**: React  
**Infra**: AWS EC2, Docker, Jenkins  


## ✨ Main Features
### 📦 재고 관리
- 실시간 재고 현황 조회
- 동시 요청 100건 시 재고 오차 0건 (비관적 락)
 <img width="1407" height="745" alt="스크린샷 2026-03-04 211727" src="https://github.com/user-attachments/assets/20ba6c34-3597-4724-8caa-a15851c0ddeb" />

### 📊 대시보드
- Redis 캐싱 적용 응답 속도 99% 개선 (1,408ms → 1ms)
- N+1 문제 해결 (쿼리 2N회 → 2회)
<img width="1396" height="935" alt="스크린샷 2026-03-04 211654" src="https://github.com/user-attachments/assets/a8ec96c1-3e13-4cd7-92da-a30172426e34" />


### 🚚 주문/배송 관리
- 주문 → 결제 → 조립 → 배송 통합 관리
- 배송 추적 API 연동
- 조립 프로세스 단계별 기록
<img width="1395" height="848" alt="스크린샷 2026-03-04 211900" src="https://github.com/user-attachments/assets/bdb79a38-ca0b-4ee1-a2da-1465e01f1181" />
