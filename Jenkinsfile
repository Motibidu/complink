pipeline {
    agent any

    environment {
        ENV_CREDENTIAL_ID = 'pcgear-prod-env'
    }

    stages {

        // 1. Cleanup workspace (Docker가 생성한 디렉토리 제거)
        stage('1. Cleanup Workspace') {
            steps {
                echo "Cleaning up Docker-created directories..."
                sh '''
                    # Docker가 자동 생성한 디렉토리 제거
                    rm -rf BackEnd/pcgear/monitoring/prometheus/prometheus.yml
                    rm -rf BackEnd/pcgear/monitoring/prometheus/alerts.yml
                    rm -rf BackEnd/pcgear/monitoring/grafana
                '''
            }
        }

        // 1-1. Verify monitoring files (Git checkout 검증)
        stage('1-1. Verify Monitoring Files') {
            steps {
                echo "Verifying monitoring configuration files..."
                sh '''
                    # prometheus.yml이 파일인지 확인
                    if [ ! -f BackEnd/pcgear/monitoring/prometheus/prometheus.yml ]; then
                        echo "ERROR: prometheus.yml is missing or is a directory!"
                        echo "Listing monitoring directory:"
                        ls -la BackEnd/pcgear/monitoring/prometheus/ || true

                        echo "Attempting to restore from git..."
                        # Git에서 파일 복구 시도
                        if [ -d .git ]; then
                            git checkout HEAD -- BackEnd/pcgear/monitoring/
                        else
                            echo "ERROR: Not a git repository. Please check Jenkins SCM configuration."
                            exit 1
                        fi
                    fi

                    # alerts.yml 확인
                    if [ ! -f BackEnd/pcgear/monitoring/prometheus/alerts.yml ]; then
                        echo "ERROR: alerts.yml is missing!"
                        exit 1
                    fi

                    # 파일 내용이 있는지 확인
                    if [ ! -s BackEnd/pcgear/monitoring/prometheus/prometheus.yml ]; then
                        echo "ERROR: prometheus.yml is empty!"
                        exit 1
                    fi

                    echo "✓ All monitoring files verified successfully"
                    cat BackEnd/pcgear/monitoring/prometheus/prometheus.yml | head -5
                '''
            }
        }

        // 2. .env 파일 생성
        stage('2. Prepare Environment File') {
            steps {
                echo "Loading production secrets..."
                withCredentials([file(credentialsId: ENV_CREDENTIAL_ID, variable: 'ENV_FILE')]) {
                    sh 'cp $ENV_FILE .env'
                }
            }
        }

        // 📌 [추가된 단계] Spring Boot 앱(JAR 파일)을 빌드합니다.
        stage('3. Build Spring Boot App') {
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

        // 4. Docker 이미지 빌드 (이제 JAR 파일이 존재합니다)
        stage('4. Build Docker Images') {
            steps {
                echo 'Building backend and frontend Docker images...'
                sh 'docker-compose build --no-cache'
            }
        }

        // 5. 애플리케이션 배포
        stage('5. Deploy Application Stack') {
            steps {
                echo 'Stopping and removing old containers (if any)...'
                sh 'docker-compose down'

                echo 'Re-cleaning Docker-created directories after down...'
                sh '''
                    # docker-compose down 후 다시 생성된 디렉토리 제거
                    rm -rf BackEnd/pcgear/monitoring/prometheus/prometheus.yml
                    rm -rf BackEnd/pcgear/monitoring/prometheus/alerts.yml
                    rm -rf BackEnd/pcgear/monitoring/grafana
                '''

                echo 'Starting all services...'
                sh 'docker-compose up -d'
            }
        }
        
        // 6. (선택적) EC2 서버 용량 확보
        stage('6. Clean Docker System') {
            steps {
                echo 'Cleaning up dangling Docker images...'
                sh 'docker image prune -f'
            }
        }
    }
    
    // 7. (보안 필수) 작업 완료 후 항상 실행
    post {
        always {
            echo 'Cleaning up secrets...'
            sh 'rm -f .env'
        }
    }
}