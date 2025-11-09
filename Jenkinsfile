pipeline {
    agent any
    
    environment {
        // Docker Hub credentials (à configurer dans Jenkins)
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        
        // Images Docker
        BACKEND_IMAGE = "your-dockerhub-username/commercial-pfe-backend"
        FRONTEND_IMAGE = "your-dockerhub-username/commercial-pfe-frontend"
        
        // Versions
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        LATEST_TAG = "latest"
        
        // Paths
        BACKEND_DIR = "."
        FRONTEND_DIR = "app-frontend-new"
        
        // Test reports
        BACKEND_TEST_REPORT = "target/surefire-reports"
        FRONTEND_TEST_REPORT = "app-frontend-new/coverage"
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 1, unit: 'HOURS')
    }
    
    stages {
        stage('🔍 Checkout') {
            steps {
                script {
                    echo "📥 Cloning repository..."
                    checkout scm
                    sh 'git rev-parse --short HEAD > .git/commit-id'
                    env.GIT_COMMIT_SHORT = readFile('.git/commit-id').trim()
                }
            }
        }
        
        stage('🏗️ Build & Test Backend') {
            steps {
                script {
                    echo "🔨 Building Backend..."
                    dir(BACKEND_DIR) {
                        // Clean et compile
                        sh 'mvn clean compile'
                        
                        // Exécuter les tests unitaires
                        echo "🧪 Running Backend Unit Tests..."
                        sh 'mvn test'
                        
                        // Package l'application
                        echo "📦 Packaging Backend..."
                        sh 'mvn package -DskipTests'
                    }
                }
            }
            post {
                always {
                    // Publier les résultats des tests JUnit
                    junit "${BACKEND_TEST_REPORT}/**/*.xml"
                    
                    // Publier le rapport de couverture (si JaCoCo est configuré)
                    jacoco(
                        execPattern: 'target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java'
                    )
                }
            }
        }
        
        stage('🎨 Build & Test Frontend') {
            steps {
                script {
                    echo "🔨 Building Frontend..."
                    dir(FRONTEND_DIR) {
                        // Installer les dépendances
                        sh 'npm ci --legacy-peer-deps'
                        
                        // Linter
                        echo "🔍 Running ESLint..."
                        sh 'npm run lint || true'
                        
                        // Tests unitaires avec couverture
                        echo "🧪 Running Frontend Unit Tests..."
                        sh 'npm run test -- --watch=false --code-coverage --browsers=ChromeHeadless'
                        
                        // Build production
                        echo "📦 Building Frontend for Production..."
                        sh 'npm run build -- --configuration production'
                    }
                }
            }
            post {
                always {
                    // Publier les résultats des tests Karma
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: "${FRONTEND_DIR}/coverage",
                        reportFiles: 'index.html',
                        reportName: 'Frontend Coverage Report'
                    ])
                }
            }
        }
        
        stage('🔒 Security Scan') {
            parallel {
                stage('Backend Security') {
                    steps {
                        script {
                            echo "🔐 Scanning Backend Dependencies..."
                            dir(BACKEND_DIR) {
                                // OWASP Dependency Check
                                sh 'mvn org.owasp:dependency-check-maven:check || true'
                            }
                        }
                    }
                }
                stage('Frontend Security') {
                    steps {
                        script {
                            echo "🔐 Scanning Frontend Dependencies..."
                            dir(FRONTEND_DIR) {
                                // npm audit
                                sh 'npm audit --audit-level=moderate || true'
                            }
                        }
                    }
                }
            }
        }
        
        stage('🐳 Build Docker Images') {
            parallel {
                stage('Backend Image') {
                    steps {
                        script {
                            echo "🐳 Building Backend Docker Image..."
                            dir(BACKEND_DIR) {
                                docker.build("${BACKEND_IMAGE}:${IMAGE_TAG}")
                                docker.build("${BACKEND_IMAGE}:${LATEST_TAG}")
                            }
                        }
                    }
                }
                stage('Frontend Image') {
                    steps {
                        script {
                            echo "🐳 Building Frontend Docker Image..."
                            dir(FRONTEND_DIR) {
                                docker.build("${FRONTEND_IMAGE}:${IMAGE_TAG}")
                                docker.build("${FRONTEND_IMAGE}:${LATEST_TAG}")
                            }
                        }
                    }
                }
            }
        }
        
        stage('🧪 Integration Tests') {
            steps {
                script {
                    echo "🧪 Running Integration Tests..."
                    
                    // Démarrer les conteneurs pour les tests
                    sh '''
                        docker-compose -f docker-compose.test.yml up -d
                        sleep 30
                    '''
                    
                    // Exécuter les tests d'intégration backend
                    dir(BACKEND_DIR) {
                        sh 'mvn verify -Dtest=*Integration* || true'
                    }
                    
                    // Exécuter les tests E2E frontend (si configurés)
                    dir(FRONTEND_DIR) {
                        sh 'npm run e2e || true'
                    }
                }
            }
            post {
                always {
                    // Arrêter les conteneurs de test
                    sh 'docker-compose -f docker-compose.test.yml down -v'
                }
            }
        }
        
        stage('📤 Push Docker Images') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "📤 Pushing Docker Images to Registry..."
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                        // Push Backend
                        docker.image("${BACKEND_IMAGE}:${IMAGE_TAG}").push()
                        docker.image("${BACKEND_IMAGE}:${LATEST_TAG}").push()
                        
                        // Push Frontend
                        docker.image("${FRONTEND_IMAGE}:${IMAGE_TAG}").push()
                        docker.image("${FRONTEND_IMAGE}:${LATEST_TAG}").push()
                    }
                }
            }
        }
        
        stage('🚀 Deploy to Staging') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "🚀 Deploying to Staging Environment..."
                    
                    // Déployer avec docker-compose
                    sh """
                        export BACKEND_IMAGE=${BACKEND_IMAGE}:${IMAGE_TAG}
                        export FRONTEND_IMAGE=${FRONTEND_IMAGE}:${IMAGE_TAG}
                        docker-compose -f docker-compose.staging.yml up -d
                    """
                    
                    // Attendre que les services soient prêts
                    sh 'sleep 30'
                    
                    // Health check
                    sh '''
                        curl -f http://localhost:8080/actuator/health || exit 1
                        curl -f http://localhost:80 || exit 1
                    '''
                }
            }
        }
        
        stage('✅ Smoke Tests') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "✅ Running Smoke Tests..."
                    
                    // Tests de base pour vérifier que l'application fonctionne
                    sh '''
                        # Test Backend API
                        curl -f http://localhost:8080/api/health || exit 1
                        
                        # Test Frontend
                        curl -f http://localhost:80 || exit 1
                        
                        # Test MongoDB connection
                        docker exec mongodb mongo --eval "db.adminCommand('ping')" || exit 1
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo "✅ Pipeline completed successfully!"
            // Notification Slack/Email
            // slackSend(color: 'good', message: "Build ${env.BUILD_NUMBER} succeeded")
        }
        failure {
            echo "❌ Pipeline failed!"
            // Notification Slack/Email
            // slackSend(color: 'danger', message: "Build ${env.BUILD_NUMBER} failed")
        }
        always {
            // Nettoyer les images Docker locales
            sh '''
                docker image prune -f
                docker container prune -f
            '''
            
            // Archiver les artifacts
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/dist/**/*', allowEmptyArchive: true
        }
    }
}
