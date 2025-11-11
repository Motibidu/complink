pipeline {
    // Jenkins가 설치된 호스트(EC2)의 Docker를 사용합니다.
    agent any

    // 1. Jenkins Credentials에 등록해야 할 ID 목록
    environment {
        // (필수) docker-compose.yml이 사용할 .env 파일의 Credential ID
        // Jenkins > Manage Jenkins > Credentials > System > Global credentials
        // Kind: "Secret file", ID: "pcgear-prod-env" (이 이름으로 생성해야 함)
        ENV_CREDENTIAL_ID = 'pcgear-prod-env'
    }

    stages {
        // 2. GitHub에서 소스 코드 가져오기
        stage('1. Checkout Source') {
            steps {
                echo 'Checking out source code...'
                // 작업 공간을 정리합니다.
                cleanWs() 
            }
        }

        // 3. docker-compose.yml이 사용할 .env 파일 준비
        stage('2. Prepare Environment File') {
            steps {
                echo "Loading production secrets..."
                // Jenkins에 등록된 "Secret file" (pcgear-prod-env)을
                // $ENV_FILE 변수로 로드합니다.
                withCredentials([file(credentialsId: ENV_CREDENTIAL_ID, variable: 'ENV_FILE')]) {
                    
                    echo "Creating .env file for docker-compose..."
                    sh 'cp $ENV_FILE .env'
                }
            }
        }

        // 4. Docker 이미지 빌드
        stage('3. Build Docker Images') {
            steps {
                echo 'Building backend and frontend Docker images...'
                
                // docker-compose.yml에 정의된 'build' 섹션을 실행
                // (backend, frontend)
                sh 'docker-compose build --no-cache'
            }
        }

        // 5. 애플리케이션 배포
        stage('4. Deploy Application Stack') {
            steps {
                echo 'Stopping and removing old containers (if any)...'
                // 기존 컨테이너 중지 및 제거
                sh 'docker-compose down'
                
                echo 'Starting all services (db, redis, backend, frontend)...'
                // .env 파일을 사용하여 모든 서비스를 백그라운드로 시작
                sh 'docker-compose up -d'
            }
        }
        
        // 6. (선택적) EC2 서버 용량 확보
        stage('5. Clean Docker System') {
            steps {
                echo 'Cleaning up dangling Docker images...'
                // 빌드 과정에서 사용된 중간 이미지(dangling images)를 삭제
                sh 'docker image prune -f'
            }
        }
    }
    
    // 7. (보안 필수) 작업 완료 후 항상 실행
    post {
        always {
            echo 'Cleaning up secrets...'
            //젠킨스 파이프라인이 빌드 과정에서 '임시'로 생성했던 .env 파일을 삭제합니다.
            sh 'rm -f .env'
        }
    }
}