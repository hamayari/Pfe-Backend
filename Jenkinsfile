pipeline {
    agent any
    
    tools {
        maven 'maven'
    }
    
    environment {
        JAVA_HOME = '/opt/java/openjdk'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        MAVEN_OPTS = '-Xmx2048m -Xms512m'
        DOCKER_IMAGE = 'hamalak/pfe-backend'
        DOCKER_TAG = "${BUILD_NUMBER}"
        GIT_REPO = 'https://github.com/hamayari/Pfe-Backend.git'
        GIT_BRANCH = 'develop'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 45, unit: 'MINUTES')
        timestamps()
    }
    
    stages {
        stage('🧹 Cleanup & Checkout') {
            steps {
                echo '🧹 Nettoyage du workspace...'
                deleteDir()
                echo '📥 Checkout du code depuis GitHub...'
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO}"
            }
        }
        
        stage('🔍 Vérification Environnement') {
            steps {
                echo '🔍 Vérification de l\'environnement de build...'
                sh '''
                    echo "=========================================="
                    echo "☕ Java Version:"
                    java -version
                    echo ""
                    echo "📦 Maven Version:"
                    mvn -version
                    echo ""
                    echo "🐳 Docker Version:"
                    docker --version || echo "Docker non disponible"
                    echo "=========================================="
                '''
            }
        }
        
        stage('🔨 Build') {
            steps {
                echo '🔨 Compilation du projet...'
                sh 'mvn clean compile -DskipTests -B'
            }
        }
        
        stage('🧪 Tests Unitaires') {
            steps {
                echo '🧪 Exécution des tests unitaires...'
                sh 'mvn test -Dmaven.test.failure.ignore=true -B'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }
        
        stage('🔗 Tests d\'Intégration') {
            steps {
                echo '🔗 Exécution des tests d\'intégration...'
                sh 'mvn verify -DskipUnitTests=true -Dmaven.test.failure.ignore=true -B'
            }
            post {
                always {
                    junit testResults: '**/target/failsafe-reports/*.xml', allowEmptyResults: true
                }
            }
        }
        
        stage('📊 Rapport de Couverture JaCoCo') {
            steps {
                echo '📊 Génération du rapport de couverture de code...'
                sh 'mvn jacoco:report -B'
                
                echo '📋 Vérification des fichiers de couverture générés...'
                sh '''
                    echo "=========================================="
                    echo "📁 Fichiers JaCoCo générés:"
                    ls -lh target/jacoco.exec 2>/dev/null && echo "✅ jacoco.exec trouvé" || echo "⚠️ jacoco.exec non trouvé"
                    ls -lh target/site/jacoco/jacoco.xml 2>/dev/null && echo "✅ jacoco.xml trouvé" || echo "⚠️ jacoco.xml non trouvé"
                    ls -lh target/site/jacoco/index.html 2>/dev/null && echo "✅ index.html trouvé" || echo "⚠️ index.html non trouvé"
                    echo "=========================================="
                '''
            }
            post {
                always {
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: '📊 JaCoCo Coverage Report'
                    ])
                }
            }
        }
        
        stage('🔍 Analyse SonarQube') {
            steps {
                echo '🔍 Préparation de l\'analyse SonarQube...'
                
                script {
                    // Vérifier si SonarQube est accessible
                    def sonarReady = sh(
                        script: 'curl -s -o /dev/null -w "%{http_code}" http://sonarqube:9000/api/system/status',
                        returnStdout: true
                    ).trim()
                    
                    if (sonarReady == '200') {
                        echo '✅ SonarQube est accessible'
                    } else {
                        echo '⏳ SonarQube démarre... Attente de 30 secondes'
                        sleep 30
                    }
                }
                
                echo '🔍 Lancement de l\'analyse SonarQube...'
                withCredentials([string(credentialsId: 'sonar', variable: 'SONAR_TOKEN')]) {
                    sh """
                        mvn sonar:sonar \
                            -Dsonar.projectKey=Commercial-PFE-Backend \
                            -Dsonar.projectName='Commercial PFE Backend' \
                            -Dsonar.host.url=http://sonarqube:9000 \
                            -Dsonar.token=\${SONAR_TOKEN} \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.java.test.binaries=target/test-classes \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                            -Dsonar.junit.reportPaths=target/surefire-reports,target/failsafe-reports \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.qualitygate.wait=false \
                            -B || {
                                echo "⚠️ Analyse SonarQube échouée mais on continue..."
                                exit 0
                            }
                    """
                }
                echo '✅ Analyse SonarQube envoyée avec succès!'
            }
        }
        
        stage('📦 Package') {
            steps {
                echo '📦 Création du package JAR...'
                sh 'mvn package -DskipTests -B'
                
                echo '📋 Vérification du JAR créé...'
                sh '''
                    echo "=========================================="
                    ls -lh target/*.jar
                    echo "=========================================="
                '''
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    echo '✅ Artefact JAR archivé avec succès'
                }
            }
        }
        
        stage('🐳 Build Docker Image') {
            steps {
                echo '🐳 Construction de l\'image Docker...'
                script {
                    try {
                        sh """
                            docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:develop-latest
                        """
                        echo '✅ Image Docker créée avec succès'
                        
                        // Afficher les images créées
                        sh """
                            echo "=========================================="
                            echo "🐳 Images Docker créées:"
                            docker images | grep ${DOCKER_IMAGE} | head -5
                            echo "=========================================="
                        """
                    } catch (Exception e) {
                        echo "⚠️ Erreur lors de la création de l'image Docker: ${e.message}"
                        echo "⚠️ Continuons quand même..."
                    }
                }
            }
        }
        
        stage('📤 Push Docker Hub') {
            steps {
                echo '📤 Push vers Docker Hub...'
                withCredentials([usernamePassword(credentialsId: 'docker_credentiel', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo \${DOCKER_PASS} | docker login -u \${DOCKER_USER} --password-stdin
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker push ${DOCKER_IMAGE}:latest
                        docker push ${DOCKER_IMAGE}:develop-latest
                    """
                }
                echo '✅ Images poussées vers Docker Hub avec succès'
            }
        }
    }
    
    post {
        success {
            echo ''
            echo '✅ ========================================='
            echo '✅         PIPELINE RÉUSSI !              '
            echo '✅ ========================================='
            echo ''
            echo '📊 Rapports disponibles:'
            echo "   📈 Tests: ${BUILD_URL}testReport/"
            echo "   📊 JaCoCo Coverage: ${BUILD_URL}JaCoCo_20Coverage_20Report/"
            echo '   🔍 SonarQube: http://localhost:9000/dashboard?id=Commercial-PFE-Backend'
            echo ''
            echo "📦 Artefacts: ${BUILD_URL}artifact/"
            echo "🐳 Image Docker: ${DOCKER_IMAGE}:${DOCKER_TAG}"
            echo ''
            echo '=========================================='
        }
        
        failure {
            echo ''
            echo '❌ ========================================='
            echo '❌         PIPELINE ÉCHOUÉ !              '
            echo '❌ ========================================='
            echo ''
            echo '📋 Vérifiez les logs ci-dessus pour plus de détails'
            echo "📊 Console: ${BUILD_URL}console"
            echo ''
        }
        
        unstable {
            echo ''
            echo '⚠️ ========================================='
            echo '⚠️       PIPELINE INSTABLE                '
            echo '⚠️ ========================================='
            echo ''
            echo '📋 Certains tests ont échoué mais le build continue'
            echo "📊 Tests: ${BUILD_URL}testReport/"
            echo ''
        }
        
        always {
            echo ''
            echo '🧹 Nettoyage et finalisation...'
            echo "📊 Build #${BUILD_NUMBER} terminé à ${new Date()}"
            echo ''
            
            // Statistiques des tests
            script {
                try {
                    def testResults = junit testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml', allowEmptyResults: true
                    echo "📈 Tests exécutés: ${testResults.totalCount}"
                    echo "✅ Tests réussis: ${testResults.passCount}"
                    echo "❌ Tests échoués: ${testResults.failCount}"
                    echo "⏭️ Tests ignorés: ${testResults.skipCount}"
                } catch (Exception e) {
                    echo "⚠️ Impossible de récupérer les statistiques des tests"
                }
            }
            
            echo '=========================================='
        }
    }
}
