pipeline {
    agent any

    environment {
        ENV_CREDENTIAL_ID = 'pcgear-prod-env'
    }

    stages {
        
        // 1. .env 파일 생성
        stage('1. Prepare Environment File') {
            steps {
                echo "Loading production secrets..."
                withCredentials([file(credentialsId: ENV_CREDENTIAL_ID, variable: 'ENV_FILE')]) {
                    sh 'cp $ENV_FILE .env'
                }
            }
        }

        // 📌 [추가된 단계] Spring Boot 앱(JAR 파일)을 빌드합니다.
        stage('2. Build Spring Boot App') {
            steps {
                echo 'Building Spring Boot JAR file...'
                
                // 1. BackEnd/pcgear 프로젝트 폴더로 이동합니다.
                dir('BackEnd/pcgear') {
                    // 2. gradlew 파일에 실행 권한을 부여합니다.
                    sh 'chmod +x ./gradlew'
                    
                    // 3. Gradle 빌드를 실행합니다. (JAR 파일 생성)
                    sh './gradlew clean build'
                }
            }
        }

        // 3. Docker 이미지 빌드 (이제 JAR 파일이 존재합니다)
        stage('3. Build Docker Images') {
            steps {
                echo 'Building backend and frontend Docker images...'
                sh 'docker-compose build --no-cache'
            }
        }

        // 4. 애플리케이션 배포
        stage('4. Deploy Application Stack') {
            steps {
                echo 'Stopping and removing old containers (if any)...'
                sh 'docker-compose down'
                
                echo 'Starting all services...'
                sh 'docker-compose up -d'
            }
        }
        
        // 5. (선택적) EC2 서버 용량 확보
        stage('5. Clean Docker System') {
            steps {
                echo 'Cleaning up dangling Docker images...'
                sh 'docker image prune -f'
            }
        }
    }
    
    // 6. (보안 필수) 작업 완료 후 항상 실행
    post {
        always {
            echo 'Cleaning up secrets...'
            sh 'rm -f .env'
        }
    }
}