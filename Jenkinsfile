pipeline {
    agent any
    
    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        DOCKER_REGISTRY = 'localhost:5000'
        PROJECT_NAME = 'arqsoft'
        DEV_PORT = '8081'
        STAGING_PORT = '8082'
        PROD_PORT = '8083'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }
    
    triggers {
        pollSCM('H/15 * * * *')
    }
    
    stages {
        stage('Checkout & Info') {
            steps {
                script {
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Build Number: ${env.BUILD_NUMBER}"
                    echo "Git Commit: ${env.GIT_COMMIT}"
                }
            }
        }
        
        stage('Environment Setup') {
            steps {
                script {
                    echo "Setting up build environment..."
                    echo "Java Version: ${sh(script: 'java -version', returnStdout: true)}"
                    echo "Maven Version: ${sh(script: 'mvn --version', returnStdout: true)}"
                    
                    sh 'mkdir -p target/surefire-reports'
                    sh 'mkdir -p target/failsafe-reports'
                    sh 'mkdir -p target/site/jacoco'
                    sh 'mkdir -p target/pit-reports'
                }
            }
        }
        
        stage('Compile') {
            steps {
                script {
                    echo "Compiling the application..."
                    sh 'mvn clean compile -DskipTests'
                    
                    archiveArtifacts artifacts: 'target/classes/**', allowEmptyArchive: true
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(installationName: 'arqsoftsonar') {
                    script {
                        echo "Running SonarQube analysis..."
                        sh '''
                            mvn sonar:sonar \
                                -Dsonar.projectKey=${PROJECT_NAME} \
                                -Dsonar.projectName=${PROJECT_NAME} \
                                -Dsonar.java.binaries=target/classes \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        '''
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES'){
                    waitForQualityGate abortPipeline: false
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                script {
                    echo "Running unit tests..."
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                script {
                    echo "Running integration tests..."
                    sh 'mvn verify -DskipUTs=true'
                }
            }
            post {
                always {
                    junit 'target/failsafe-reports/*.xml'
                }
            }
        }
        
        stage('Test Coverage') {
            steps {
                script {
                    echo "Generating test coverage reports..."
                    sh 'mvn jacoco:report'
                    
                    echo "Checking coverage thresholds..."
                    try {
                        sh 'mvn jacoco:check'
                        echo "Coverage check passed!"
                    } catch (Exception e) {
                        echo "Coverage check failed, but continuing pipeline: ${e.getMessage()}"
                    }
                }
            }
            post {
                always {
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                    ])
                }
            }
        }
        
        /*stage('Mutation Testing') {
             steps {
            script {
                 echo "Running mutation tests..."
                 try {
                 sh 'mvn org.pitest:pitest-maven:mutationCoverage'
                 } catch (Exception e) {
                 echo "Mutation tests failed, but continuing pipeline: ${e.getMessage()}"
                }
             }
             }
             post {
             always {
                 publishHTML([
                 allowMissing: true,
                 alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/pit-reports',
                 reportFiles: 'index.html',
                 reportName: 'PIT Mutation Testing Report'
                ])
             }
             }
         }*/
        
        stage('Package Application') {
            steps {
                script {
                    echo "Creating application package..."
                    sh 'mvn package -DskipTests'

                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        
        stage('Build Docker Image') {
            when {
                expression { 
                    return env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == 'staging' || env.BRANCH_NAME == 'main'
                }
            }
            steps {
                script {
                    echo "Building Docker image: ${PROJECT_NAME}:${BUILD_NUMBER}"
                    sh """
                        docker build \
                            -t ${PROJECT_NAME}:${BUILD_NUMBER} \
                            -t ${PROJECT_NAME}:${env.BRANCH_NAME}-latest \
                            --label branch=${env.BRANCH_NAME} \
                            --label build=${BUILD_NUMBER} \
                            .
                    """
                    
                    echo "Docker image built successfully: ${PROJECT_NAME}:${BUILD_NUMBER}"
                }
            }
        }
        
        stage('Deploy to DEV') {
            when {
                expression { env.BRANCH_NAME == 'dev' }
            }
            steps {
                script {
                    echo "Deploying to DEV environment"
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Container: arqsoft-dev"
                    echo "Port: ${env.DEV_PORT}"
                    echo "Image: ${PROJECT_NAME}:${BUILD_NUMBER}"

                    sh '''
                        docker network inspect aqrsoft_arqsoft-network >/dev/null 2>&1 || \
                        docker network create aqrsoft_arqsoft-network
                    '''
                    
                    sh '''
                        docker stop arqsoft-dev || true
                        docker rm arqsoft-dev || true
                    '''
                    
                    sh """
                        docker run -d \
                            --name arqsoft-dev \
                            -p ${env.DEV_PORT}:8081 \
                            --network aqrsoft_arqsoft-network \
                            --restart unless-stopped \
                            -e SPRING_PROFILES_ACTIVE=dev,bootstrap \
                            --label branch=${env.BRANCH_NAME} \
                            --label environment=dev \
                            --label build=${BUILD_NUMBER} \
                            ${PROJECT_NAME}:${BUILD_NUMBER}
                    """
                    
                    echo "Application deployed to DEV environment"
                    echo "Access: http://localhost:${env.DEV_PORT}"
                }
            }
        }
        
        stage('System Tests DEV') {
            when {
                expression { env.BRANCH_NAME == 'dev' }
            }
            steps {
                script {
                    echo "Running system tests against DEV environment..."
                    
                    try {
                        echo "Running k6 Author System Tests..."
                        sh 'k6 run src/test/java/pt/psoft/g1/psoftg1/system/author-system-test.js'
                        echo "k6 Author tests passed!"
                    } catch (Exception e) {
                        echo "k6 Author tests failed, but continuing pipeline: ${e.getMessage()}"
                    }
                    
                    try {
                        echo "Running k6 Book System Tests..."
                        sh 'k6 run src/test/java/pt/psoft/g1/psoftg1/system/book-system-test.js'
                        echo "k6 Book tests passed!"
                    } catch (Exception e) {
                        echo "k6 Book tests failed, but continuing pipeline: ${e.getMessage()}"
                    }
                    
                    echo "System tests completed for DEV"
                }
            }
        }

        stage('Deploy to STAGING') {
            when {
                expression { env.BRANCH_NAME == 'staging' }
            }
            steps {
                script {
                    echo "Deploying to STAGING environment"
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Container: arqsoft-staging"
                    echo "Port: ${env.STAGING_PORT}"
                    echo "Image: ${PROJECT_NAME}:${BUILD_NUMBER}"
                    
                    sh '''
                        docker network inspect aqrsoft_arqsoft-network >/dev/null 2>&1 || \
                        docker network create aqrsoft_arqsoft-network
                    '''
                    
                    sh '''
                        docker stop arqsoft-staging || true
                        docker rm arqsoft-staging || true
                    '''
                    
                    sh """
                        docker run -d \
                            --name arqsoft-staging \
                            -p ${env.STAGING_PORT}:8081 \
                            --network aqrsoft_arqsoft-network \
                            --restart unless-stopped \
                            -e SPRING_PROFILES_ACTIVE=staging,bootstrap \
                            --label branch=${env.BRANCH_NAME} \
                            --label environment=staging \
                            --label build=${BUILD_NUMBER} \
                            ${PROJECT_NAME}:${BUILD_NUMBER}
                    """
                    
                    echo "Application deployed to STAGING environment"
                    echo "Access: http://localhost:${env.STAGING_PORT}"
                }
            }
        }

        stage('System Tests STAGING') {
            when {
                expression { env.BRANCH_NAME == 'staging' }
            }
            steps {
                script {
                    echo "Running system tests against STAGING with partial real data..."
                    

                    try {
                        echo "Running k6 Author System Tests..."
                        sh 'BASE_URL=http://localhost:8085/api k6 run src/test/java/pt/psoft/g1/psoftg1/system/author-system-test.js'
                        echo "k6 Author tests passed!"
                    } catch (Exception e) {
                        echo "k6 Author tests failed, but continuing pipeline: ${e.getMessage()}"
                    }

                    try {
                        echo "Running k6 Book System Tests..."
                        sh 'BASE_URL=http://localhost:8085/api k6 run src/test/java/pt/psoft/g1/psoftg1/system/book-system-test.js'
                        echo "k6 Book tests passed!"
                    } catch (Exception e) {
                        echo "k6 Book tests failed, but continuing pipeline: ${e.getMessage()}"
                    }
                    
                    echo "System tests completed for STAGING"
                }
            }
        }

        stage('Deploy to PROD') {
            when {
                expression { env.BRANCH_NAME == 'main' }
            }
            steps {
                script {
                    echo "Deploying to PRODUCTION environment"
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Container: arqsoft-prod"
                    echo "Port: ${env.PROD_PORT}"
                    echo "Image: ${PROJECT_NAME}:${BUILD_NUMBER}"

                    sh '''
                        docker network inspect aqrsoft_arqsoft-network >/dev/null 2>&1 || \
                        docker network create aqrsoft_arqsoft-network
                    '''
                    
                    sh '''
                        docker stop arqsoft-prod || true
                        docker rm arqsoft-prod || true
                    '''
                    
                    sh """
                        docker run -d \
                            --name arqsoft-prod \
                            -p ${env.PROD_PORT}:8081 \
                            --network aqrsoft_arqsoft-network \
                            --restart unless-stopped \
                            -e SPRING_PROFILES_ACTIVE=prod,mongodb-redis \
                            --label branch=${env.BRANCH_NAME} \
                            --label environment=prod \
                            --label build=${BUILD_NUMBER} \
                            ${PROJECT_NAME}:${BUILD_NUMBER}
                    """
                    
                    echo "Application deployed to PRODUCTION environment"
                    echo "Access: http://localhost:${env.PROD_PORT}"
                }
            }
        }
    
    }
    
    post {
        always {
            script {
                echo "Pipeline completed. Cleaning up..."
                
                echo "Cleaning up old Docker images..."
                sh '''

                    docker images -f "dangling=true" -q | xargs -r docker rmi || true
                    
                    docker images ${PROJECT_NAME} --format "{{.Tag}}" | grep -E "^[0-9]+$" | sort -rn | tail -n +6 | xargs -r -I {} docker rmi ${PROJECT_NAME}:{} || true
                '''
                echo "Docker cleanup completed"
                
                archiveArtifacts artifacts: 'target/surefire-reports/**', allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/failsafe-reports/**', allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/pit-reports/**', allowEmptyArchive: true
            }
        }
        
        success {
            script {
                echo "Pipeline succeeded!"
            }
        }
        
        failure {
            script {
                echo "Pipeline failed!"
            }
        }
        
        cleanup {
            script {
                echo "Cleaning up workspace..."
                sh 'rm -rf ./*'
            }
        }
    }
}