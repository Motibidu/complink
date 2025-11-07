# 1. 빌드(Build) 스테이지: React 앱을 빌드합니다.
FROM node:20-alpine AS builder
WORKDIR /app

ARG VITE_PORTONE_STORE_ID
ARG VITE_PORTONE_TOSSPAY_CHANNEL_KEY

# package.json만 먼저 복사하여 의존성 캐싱 활용
COPY package.json ./
COPY package-lock.json ./
RUN npm install

ENV VITE_PORTONE_STORE_ID=$VITE_PORTONE_STORE_ID
ENV VITE_PORTONE_TOSSPAY_CHANNEL_KEY=$VITE_PORTONE_TOSSPAY_CHANNEL_KEY

# 소스 코드 전체 복사
COPY . ./

# React 앱 빌드
RUN npm run build

# 2. 프로덕션(Production) 스테이지: 빌드된 파일을 Nginx로 서비스합니다.
FROM nginx:stable-alpine

# 위 'builder' 스테이지에서 빌드된 결과물을 Nginx의 기본 웹 루트로 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# Nginx 설정을 위해 아래에서 만들 설정 파일을 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Nginx는 기본적으로 80번 포트를 사용
EXPOSE 80

# 컨테이너 실행 시 Nginx 실행
CMD ["nginx", "-g", "daemon off;"]