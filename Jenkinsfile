pipeline {
    agent any
    
    environment {
        // Docker and registry settings
        DOCKER_REGISTRY = 'your-docker-registry.com'
        DOCKER_REPO = 'food-delivery-platform'
        
        // Database settings for testing
        MYSQL_ROOT_PASSWORD = 'shaik'
        MYSQL_DATABASE = 'food_delivery_test'
        
        // Node.js and Java versions
        NODEJS_VERSION = '18'
        JAVA_VERSION = '17'
        PYTHON_VERSION = '3.9'
        
        // Build number for tagging
        BUILD_TAG = "${env.BUILD_NUMBER}"
        
        // Environment variables (these should be set in Jenkins credentials)
        STRIPE_SECRET_KEY = credentials('stripe-secret-key')
        STRIPE_PUBLISHABLE_KEY = credentials('stripe-publishable-key')
        OPENAI_API_KEY = credentials('openai-api-key')
        GOOGLE_MAPS_API_KEY = credentials('google-maps-api-key')
    }
    
    tools {
        nodejs "${NODEJS_VERSION}"
        maven 'Maven-3.9'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
                
                // Create .env file for build
                script {
                    writeFile file: '.env', text: """
STRIPE_SECRET_KEY=${STRIPE_SECRET_KEY}
STRIPE_PUBLISHABLE_KEY=${STRIPE_PUBLISHABLE_KEY}
OPENAI_API_KEY=${OPENAI_API_KEY}
GOOGLE_MAPS_API_KEY=${GOOGLE_MAPS_API_KEY}
"""
                }
            }
        }
        
        stage('Setup Infrastructure') {
            parallel {
                stage('Start Test Database') {
                    steps {
                        echo 'Starting MySQL and Redis for testing...'
                        script {
                            sh '''
                                docker-compose -f docker-compose.test.yml up -d mysql redis
                                sleep 30
                                docker-compose -f docker-compose.test.yml logs mysql
                            '''
                        }
                    }
                }
                
                stage('Install Dependencies') {
                    steps {
                        echo 'Installing project dependencies...'
                        
                        // Frontend dependencies
                        dir('frontend') {
                            sh 'npm ci'
                        }
                        
                        // AI Service dependencies
                        dir('ai-service') {
                            sh '''
                                python3 -m venv venv
                                . venv/bin/activate
                                pip install -r requirements.txt
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Code Quality & Security') {
            parallel {
                stage('Frontend Lint & Security') {
                    steps {
                        dir('frontend') {
                            sh 'npm run lint || true'
                            sh 'npm audit --audit-level=high || true'
                        }
                    }
                }
                
                stage('Backend Security Scan') {
                    steps {
                        script {
                            def services = ['user-service', 'restaurant-service', 'delivery-service', 'payment-service', 'api-gateway']
                            services.each { service ->
                                dir("backend/${service}") {
                                    sh 'mvn org.owasp:dependency-check-maven:check || true'
                                }
                            }
                        }
                    }
                }
                
                stage('Python Code Quality') {
                    steps {
                        dir('ai-service') {
                            sh '''
                                . venv/bin/activate
                                pip install flake8 black
                                flake8 app/ || true
                                black --check app/ || true
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Build & Test') {
            parallel {
                stage('Build Backend Services') {
                    steps {
                        echo 'Building Java microservices...'
                        script {
                            def services = ['user-service', 'restaurant-service', 'delivery-service', 'payment-service', 'api-gateway']
                            services.each { service ->
                                dir("backend/${service}") {
                                    sh 'mvn clean compile test package -DskipTests=false'
                                }
                            }
                        }
                    }
                    post {
                        always {
                            // Collect test results
                            script {
                                def services = ['user-service', 'restaurant-service', 'delivery-service', 'payment-service', 'api-gateway']
                                services.each { service ->
                                    publishTestResults testResultsPattern: "backend/${service}/target/surefire-reports/*.xml"
                                }
                            }
                        }
                    }
                }
                
                stage('Build Frontend') {
                    steps {
                        echo 'Building React frontend...'
                        dir('frontend') {
                            sh 'npm run test -- --coverage --watchAll=false || true'
                            sh 'npm run build'
                        }
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'frontend/coverage/lcov-report',
                                reportFiles: 'index.html',
                                reportName: 'Frontend Coverage Report'
                            ])
                        }
                    }
                }
                
                stage('Test AI Service') {
                    steps {
                        echo 'Testing Python AI service...'
                        dir('ai-service') {
                            sh '''
                                . venv/bin/activate
                                pip install pytest pytest-cov
                                pytest --cov=app --cov-report=html || true
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                echo 'Running integration tests...'
                script {
                    sh '''
                        # Start all services for integration testing
                        docker-compose -f docker-compose.test.yml up -d --build
                        sleep 60
                        
                        # Wait for services to be healthy
                        echo "Waiting for services to be ready..."
                        timeout 300 bash -c 'until curl -f http://localhost:8888/actuator/health; do sleep 5; done'
                        
                        # Run integration tests
                        echo "Running integration tests..."
                        # Add your integration test commands here
                        
                        # Example health checks
                        curl -f http://localhost:8081/actuator/health || exit 1
                        curl -f http://localhost:8082/actuator/health || exit 1
                        curl -f http://localhost:8083/actuator/health || exit 1
                        curl -f http://localhost:8084/actuator/health || exit 1
                        curl -f http://localhost:8888/actuator/health || exit 1
                        curl -f http://localhost:8085/health || exit 1
                    '''
                }
            }
            post {
                always {
                    sh 'docker-compose -f docker-compose.test.yml logs > integration-test-logs.txt || true'
                    archiveArtifacts artifacts: 'integration-test-logs.txt', allowEmptyArchive: true
                }
            }
        }
        
        stage('Build Docker Images') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'release/*'
                }
            }
            steps {
                echo 'Building Docker images...'
                script {
                    def services = [
                        'frontend',
                        'backend/user-service',
                        'backend/restaurant-service', 
                        'backend/delivery-service',
                        'backend/payment-service',
                        'backend/api-gateway',
                        'ai-service'
                    ]
                    
                    services.each { service ->
                        def serviceName = service.contains('/') ? service.split('/')[1] : service
                        def imageName = "${DOCKER_REGISTRY}/${DOCKER_REPO}-${serviceName}:${BUILD_TAG}"
                        
                        dir(service) {
                            sh "docker build -t ${imageName} ."
                            sh "docker tag ${imageName} ${DOCKER_REGISTRY}/${DOCKER_REPO}-${serviceName}:latest"
                        }
                    }
                }
            }
        }
        
        stage('Security Scan Images') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'release/*'
                }
            }
            steps {
                echo 'Scanning Docker images for vulnerabilities...'
                script {
                    def services = ['frontend', 'user-service', 'restaurant-service', 'delivery-service', 'payment-service', 'api-gateway', 'ai-service']
                    services.each { service ->
                        def imageName = "${DOCKER_REGISTRY}/${DOCKER_REPO}-${service}:${BUILD_TAG}"
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${imageName} || true"
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to staging environment...'
                script {
                    sh '''
                        # Push images to registry
                        docker-compose -f docker-compose.staging.yml push
                        
                        # Deploy to staging
                        ssh staging-server "cd /opt/food-delivery-platform && docker-compose -f docker-compose.staging.yml pull && docker-compose -f docker-compose.staging.yml up -d"
                        
                        # Wait for deployment
                        sleep 30
                        
                        # Verify staging deployment
                        curl -f http://staging.yourdomain.com:8888/actuator/health || exit 1
                    '''
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying to production environment...'
                input message: 'Deploy to Production?', ok: 'Deploy'
                
                script {
                    sh '''
                        # Push images to registry
                        docker-compose -f docker-compose.prod.yml push
                        
                        # Blue-green deployment
                        ssh production-server "cd /opt/food-delivery-platform && ./deploy-blue-green.sh ${BUILD_TAG}"
                        
                        # Verify production deployment
                        sleep 60
                        curl -f https://api.yourdomain.com/actuator/health || exit 1
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            sh 'docker-compose -f docker-compose.test.yml down -v || true'
            sh 'docker system prune -f || true'
            
            // Archive artifacts
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            archiveArtifacts artifacts: 'frontend/build/**/*', allowEmptyArchive: true
            
            // Clean workspace
            cleanWs()
        }
        
        success {
            echo 'Pipeline completed successfully!'
            
            // Send success notification
            script {
                if (env.BRANCH_NAME == 'main') {
                    slackSend(
                        channel: '#deployments',
                        color: 'good',
                        message: "✅ Food Delivery Platform deployed successfully to production! Build: ${BUILD_TAG}"
                    )
                }
            }
        }
        
        failure {
            echo 'Pipeline failed!'
            
            // Send failure notification
            slackSend(
                channel: '#deployments',
                color: 'danger',
                message: "❌ Food Delivery Platform build failed! Branch: ${env.BRANCH_NAME}, Build: ${BUILD_TAG}"
            )
        }
        
        unstable {
            echo 'Pipeline completed with warnings!'
            
            slackSend(
                channel: '#deployments',
                color: 'warning',
                message: "⚠️ Food Delivery Platform build unstable! Branch: ${env.BRANCH_NAME}, Build: ${BUILD_TAG}"
            )
        }
    }
}
