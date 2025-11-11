pipeline {
    // Jenkins가 설치된 호스트(EC2)의 Docker를 사용합니다.
    agent any

    // 1. Jenkins Credentials에 등록해야 할 ID 목록
    environment {
        // (필수) docker-compose.yml이 사용할 .env 파일의 Credential ID
        ENV_CREDENTIAL_ID = 'pcgear-prod-env'
    }

    stages {
        
        stage('1. Prepare Environment File') {
            steps {
                echo "Loading production secrets..."
                // Jenkins에 등록된 "Secret file" (pcgear-prod-env)을
                // $ENV_FILE 변수로 로드합니다.
                withCredentials([file(credentialsId: ENV_CREDENTIAL_ID, variable: 'ENV_FILE')]) {
                    
                    echo "Creating .env file for docker-compose..."
                    // SCM이 받아온 코드와 같은 위치에 .env 파일을 생성합니다.
                    sh 'cp $ENV_FILE .env'
                }
            }
        }

        // 3. Docker 이미지 빌드
        stage('2. Build Docker Images') {
            steps {
                echo 'Building backend and frontend Docker images...'
                
                // 이제 docker-compose.yml 파일이 존재하므로 정상 실행됩니다.
                sh 'docker-compose build --no-cache'
            }
        }

        // 4. 애플리케이션 배포
        stage('3. Deploy Application Stack') {
            steps {
                echo 'Stopping and removing old containers (if any)...'
                // 기존 컨테이너 중지 및 제거
                sh 'docker-compose down'
                
                echo 'Starting all services (db, redis, backend, frontend)...'
                // .env 파일을 사용하여 모든 서비스를 백그라운드로 시작
                sh 'docker-compose up -d'
            }
        }
        
        // 5. (선택적) EC2 서버 용량 확보
        stage('4. Clean Docker System') {
            steps {
                echo 'Cleaning up dangling Docker images...'
                // 빌드 과정에서 사용된 중간 이미지(dangling images)를 삭제
                sh 'docker image prune -f'
            }
        }
    }
    
    // 6. (보안 필수) 작업 완료 후 항상 실행
    post {
        always {
            echo 'Cleaning up secrets...'
            // 젠킨스 파이프라인이 빌드 과정에서 '임시'로 생성했던 .env 파일을 삭제합니다.
            sh 'rm -f .env'
        }
    }
}