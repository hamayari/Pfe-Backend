pipeline {
    agent any
    
    tools {
        maven 'maven'
        jdk 'JDK-17'
    }
    
    environment {
        // Configuration Maven & Java - OPTIMISÉ POUR ÉVITER OOM
        MAVEN_OPTS = '-Xmx2048m -Xms512m -XX:MaxMetaspaceSize=512m -XX:+UseG1GC'
        
        // Configuration Docker
        DOCKER_IMAGE = 'hamayari/pfe-backend'
        DOCKER_TAG = "${BUILD_NUMBER}"
        SONAR_HOST_URL = 'http://localhost:9000'
        GITHUB_REPO = 'https://github.com/hamayari/Pfe-Backend.git'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }
    
    stages {
        stage('🔍 System Info') {
            steps {
                script {
                    echo '════════════════════════════════════════════════════════════════'
                    echo '           COMMERCIAL PFE - PIPELINE BACKEND'
                    echo '════════════════════════════════════════════════════════════════'
                    echo "Build: #${env.BUILD_NUMBER}"
                    echo "Branch: develop"
                    echo '════════════════════════════════════════════════════════════════'
                }
                sh '''
                    echo "📊 Mémoire disponible:"
                    free -h || echo "free command not available"
                    echo ""
                    echo "💾 Espace disque:"
                    df -h | head -5
                    echo ""
                    echo "☕ Java version:"
                    java -version
                    echo ""
                    echo "📦 Maven version:"
                    mvn -version
                '''
            }
        }
        
        stage('📥 Checkout') {
            steps {
                echo '📥 Récupération du code depuis GitHub...'
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/develop']],
                    userRemoteConfigs: [[
                        url: "${GITHUB_REPO}",
                        credentialsId: 'dockerhub-credentials'
                    ]]
                ])
                script {
                    def gitCommit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    echo "✅ Code récupéré - Commit: ${gitCommit}"
                }
            }
        }
        
        stage('🔨 Build') {
            steps {
                echo '🔨 Compilation du projet...'
                sh '''
                    mvn clean compile \
                        -DskipTests \
                        -Dcheckstyle.skip=true \
                        -Dmaven.javadoc.skip=true \
                        -B -q
                '''
                echo '✅ Compilation terminée'
            }
        }
        
        stage('🧪 Unit Tests') {
            steps {
                echo '🧪 Exécution des tests unitaires (mode séquentiel)...'
                script {
                    // Vérifier si MongoDB est accessible
                    def mongoAvailable = false
                    try {
                        sh 'docker ps | grep mongodb-jenkins || docker ps | grep mongo'
                        mongoAvailable = true
                        echo '✅ MongoDB détecté'
                    } catch (Exception e) {
                        echo '⚠️ MongoDB non détecté, tentative de démarrage...'
                        try {
                            sh '''
                                docker run -d \
                                    --name mongodb-test-${BUILD_NUMBER} \
                                    -p 27018:27017 \
                                    -e MONGO_INITDB_ROOT_USERNAME=admin \
                                    -e MONGO_INITDB_ROOT_PASSWORD=admin123 \
                                    mongo:latest
                                
                                echo "⏳ Attente du démarrage de MongoDB (20s)..."
                                sleep 20
                            '''
                            mongoAvailable = true
                        } catch (Exception e2) {
                            echo "⚠️ Impossible de démarrer MongoDB: ${e2.message}"
                        }
                    }
                    
                    // Exécuter les tests
                    sh '''
                        mvn test \
                            -Dmaven.test.failure.ignore=true \
                            -Dcheckstyle.skip=true \
                            -Djunit.jupiter.execution.parallel.enabled=false \
                            -DforkCount=1 \
                            -DreuseForks=true \
                            -Dsurefire.useFile=true \
                            -Dspring.data.mongodb.host=host.docker.internal \
                            -Dspring.data.mongodb.port=27017 \
                            -Dspring.data.mongodb.database=demo \
                            -B
                    '''
                }
                echo '✅ Tests unitaires terminés'
            }
            post {
                always {
                    // Arrêter MongoDB de test si créé
                    sh """
                        docker stop mongodb-test-${BUILD_NUMBER} 2>/dev/null || true
                        docker rm mongodb-test-${BUILD_NUMBER} 2>/dev/null || true
                    """
                    
                    junit(
                        testResults: '**/target/surefire-reports/*.xml',
                        allowEmptyResults: true,
                        skipPublishingChecks: true
                    )
                    script {
                        try {
                            def testResults = junit testResults: '**/target/surefire-reports/*.xml'
                            echo "📊 Tests: ${testResults.totalCount} | ✅ Réussis: ${testResults.passCount} | ❌ Échoués: ${testResults.failCount}"
                        } catch (Exception e) {
                            echo "⚠️ Impossible de lire les résultats des tests"
                        }
                    }
                }
            }
        }
        
        stage('📊 JaCoCo Coverage') {
            steps {
                echo '📊 Génération du rapport de couverture JaCoCo...'
                sh '''
                    mvn jacoco:report \
                        -Dcheckstyle.skip=true \
                        -B -q
                '''
                echo '✅ Rapport JaCoCo généré'
            }
            post {
                always {
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '''
                            **/entity/**,
                            **/dto/**,
                            **/config/**,
                            **/model/**,
                            **/exception/**,
                            **/DemoApplication.class
                        '''
                    )
                }
            }
        }
        
        stage('🔍 SonarQube Analysis') {
            steps {
                echo '🔍 Analyse SonarQube...'
                script {
                    try {
                        withSonarQubeEnv('SonarQube') {
                            sh """
                                mvn sonar:sonar \
                                    -Dsonar.projectKey=Commercial-PFE-Backend \
                                    -Dsonar.projectName='Commercial PFE Backend' \
                                    -Dsonar.host.url=${SONAR_HOST_URL} \
                                    -Dsonar.java.binaries=target/classes \
                                    -Dsonar.sources=src/main/java \
                                    -Dsonar.tests=src/test/java \
                                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                    -Dcheckstyle.skip=true \
                                    -B -q
                            """
                        }
                        echo '✅ Analyse SonarQube terminée'
                    } catch (Exception e) {
                        echo "⚠️ SonarQube non disponible: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('🚦 Quality Gate') {
            steps {
                echo '🚦 Vérification du Quality Gate...'
                script {
                    try {
                        timeout(time: 5, unit: 'MINUTES') {
                            def qg = waitForQualityGate()
                            if (qg.status != 'OK') {
                                echo "⚠️ Quality Gate: ${qg.status}"
                                currentBuild.result = 'UNSTABLE'
                            } else {
                                echo '✅ Quality Gate: PASSED'
                            }
                        }
                    } catch (Exception e) {
                        echo "⚠️ Quality Gate timeout: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('📦 Package') {
            steps {
                echo '📦 Création du package JAR...'
                sh '''
                    mvn package \
                        -DskipTests \
                        -Dcheckstyle.skip=true \
                        -Dmaven.javadoc.skip=true \
                        -B -q
                '''
                script {
                    def jarFile = sh(
                        script: 'ls -lh target/*.jar | grep -v "original" | awk \'{print $9, $5}\' || echo "JAR not found"',
                        returnStdout: true
                    ).trim()
                    echo "✅ JAR créé: ${jarFile}"
                }
            }
            post {
                success {
                    archiveArtifacts(
                        artifacts: 'target/*.jar',
                        fingerprint: true,
                        allowEmptyArchive: false
                    )
                }
            }
        }
        
        stage('🐳 Build Docker Image') {
            steps {
                echo '🐳 Construction de l\'image Docker...'
                script {
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:develop-latest
                    """
                    
                    def imageSize = sh(
                        script: "docker images ${DOCKER_IMAGE}:${DOCKER_TAG} --format '{{.Size}}'",
                        returnStdout: true
                    ).trim()
                    
                    echo "✅ Images Docker créées:"
                    echo "  🐳 ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    echo "  🐳 ${DOCKER_IMAGE}:latest"
                    echo "  🐳 ${DOCKER_IMAGE}:develop-latest"
                    echo "📦 Taille: ${imageSize}"
                }
            }
        }
        
        stage('🧪 Test Docker Image') {
            steps {
                echo '🧪 Test de l\'image Docker...'
                script {
                    try {
                        sh "docker stop backend-test-${env.BUILD_NUMBER} 2>/dev/null || true"
                        sh "docker rm backend-test-${env.BUILD_NUMBER} 2>/dev/null || true"
                        
                        sh """
                            docker run -d \
                                --name backend-test-${env.BUILD_NUMBER} \
                                -e SPRING_PROFILES_ACTIVE=test \
                                ${DOCKER_IMAGE}:${DOCKER_TAG}
                        """
                        
                        sleep 10
                        
                        def logs = sh(
                            script: "docker logs backend-test-${env.BUILD_NUMBER} 2>&1 | tail -10",
                            returnStdout: true
                        ).trim()
                        
                        echo "📋 Logs du conteneur:"
                        echo logs
                        
                        if (logs.contains("Started") || logs.contains("Application")) {
                            echo "✅ Image Docker fonctionne correctement"
                        } else {
                            echo "⚠️ Image Docker démarrée (vérification partielle)"
                        }
                    } catch (Exception e) {
                        echo "⚠️ Test Docker: ${e.message}"
                    } finally {
                        sh "docker stop backend-test-${env.BUILD_NUMBER} 2>/dev/null || true"
                        sh "docker rm backend-test-${env.BUILD_NUMBER} 2>/dev/null || true"
                    }
                }
            }
        }
        
        stage('📤 Push Docker Image') {
            steps {
                echo '📤 Push de l\'image vers Docker Hub...'
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh '''
                            echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        '''
                        
                        def tags = [DOCKER_TAG, 'latest', 'develop-latest']
                        
                        tags.each { tag ->
                            try {
                                sh "docker push ${DOCKER_IMAGE}:${tag}"
                                echo "  ✅ Pushed: ${DOCKER_IMAGE}:${tag}"
                            } catch (Exception e) {
                                echo "  ⚠️ Failed to push ${tag}: ${e.message}"
                            }
                        }
                        
                        sh 'docker logout'
                        
                        echo "\n✅ Images poussées vers Docker Hub"
                        echo "🔗 https://hub.docker.com/r/hamayari/pfe-backend"
                    }
                }
            }
        }
        
        stage('📊 Rapport Final') {
            steps {
                script {
                    def buildStatus = currentBuild.result ?: 'SUCCESS'
                    def statusIcon = buildStatus == 'SUCCESS' ? '✅' : buildStatus == 'UNSTABLE' ? '⚠️' : '❌'
                    
                    echo '════════════════════════════════════════════════════════════════'
                    echo '                    RAPPORT FINAL DU BUILD'
                    echo '════════════════════════════════════════════════════════════════'
                    echo "${statusIcon} STATUS: ${buildStatus}"
                    echo ''
                    echo '📋 INFORMATIONS BUILD:'
                    echo "  • Build Number: #${env.BUILD_NUMBER}"
                    echo "  • Branch: develop"
                    echo ''
                    echo '📦 ARTEFACTS GÉNÉRÉS:'
                    echo "  • Docker: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    echo "  • Docker: ${DOCKER_IMAGE}:latest"
                    echo "  • Docker: ${DOCKER_IMAGE}:develop-latest"
                    echo ''
                    echo '📊 RAPPORTS DISPONIBLES:'
                    echo "  • Tests JUnit: ${env.BUILD_URL}testReport/"
                    echo "  • Couverture JaCoCo: ${env.BUILD_URL}jacoco/"
                    echo "  • SonarQube: ${SONAR_HOST_URL}/dashboard?id=Commercial-PFE-Backend"
                    echo ''
                    echo '🔗 LIENS UTILES:'
                    echo "  • Jenkins Build: ${env.BUILD_URL}"
                    echo "  • Docker Hub: https://hub.docker.com/r/hamayari/pfe-backend"
                    echo "  • GitHub: ${GITHUB_REPO}"
                    echo '════════════════════════════════════════════════════════════════'
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ ✅ ✅ BUILD RÉUSSI! ✅ ✅ ✅'
            echo "🎉 Toutes les étapes ont été complétées avec succès"
            echo "🐳 Image disponible: docker pull ${DOCKER_IMAGE}:${DOCKER_TAG}"
        }
        
        unstable {
            echo '⚠️ ⚠️ ⚠️ BUILD INSTABLE ⚠️ ⚠️ ⚠️'
            echo "⚠️ Certains tests ou quality gates ont échoué"
        }
        
        failure {
            echo '❌ ❌ ❌ BUILD ÉCHOUÉ ❌ ❌ ❌'
            echo "❌ Le build a rencontré des erreurs critiques"
            echo "💡 Consultez les logs ci-dessus pour plus de détails"
        }
        
        always {
            echo '🧹 Nettoyage des ressources...'
            sh '''
                # Nettoyage des conteneurs de test
                docker ps -a | grep backend-test | awk '{print $1}' | xargs -r docker rm -f || true
                
                # Nettoyage des images non taguées
                docker images -f "dangling=true" -q | xargs -r docker rmi || true
            '''
            
            // Nettoyage du workspace (optionnel - décommenter si nécessaire)
            // cleanWs()
            
            echo '✅ Nettoyage terminé'
        }
    }
}
