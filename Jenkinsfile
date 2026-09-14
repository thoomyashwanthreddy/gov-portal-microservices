pipeline {
    agent any

    environment {
        AWS_REGION      = 'us-east-1'
        ECR_REGISTRY    = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        BACKEND_IMAGE   = "${ECR_REGISTRY}/gov-portal-backend"
        FRONTEND_IMAGE  = "${ECR_REGISTRY}/gov-portal-frontend"
        NOTIFY_IMAGE    = "${ECR_REGISTRY}/gov-portal-notification-service"
        IMAGE_TAG       = "${env.BUILD_NUMBER}"
        KUBE_NAMESPACE  = 'gov-portal'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Test') {
            parallel {
                stage('Backend') {
                    steps {
                        dir('backend') {
                            sh 'mvn -B clean verify'
                        }
                    }
                    post {
                        always {
                            junit 'backend/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Notification Service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn -B clean verify'
                        }
                    }
                    post {
                        always {
                            junit 'notification-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Frontend') {
                    steps {
                        dir('frontend') {
                            sh 'npm ci'
                            sh 'npm run test'
                            sh 'npm run build'
                        }
                    }
                }
            }
        }

        stage('Containerize') {
            steps {
                sh "docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} -t ${BACKEND_IMAGE}:latest ./backend"
                sh "docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} -t ${FRONTEND_IMAGE}:latest ./frontend"
                sh "docker build -t ${NOTIFY_IMAGE}:${IMAGE_TAG} -t ${NOTIFY_IMAGE}:latest ./notification-service"
            }
        }

        stage('Push to ECR') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-ecr-creds']]) {
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} | \
                            docker login --username AWS --password-stdin ${ECR_REGISTRY}
                        docker push ${BACKEND_IMAGE}:${IMAGE_TAG}
                        docker push ${BACKEND_IMAGE}:latest
                        docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}
                        docker push ${FRONTEND_IMAGE}:latest
                        docker push ${NOTIFY_IMAGE}:${IMAGE_TAG}
                        docker push ${NOTIFY_IMAGE}:latest
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([file(credentialsId: 'eks-kubeconfig', variable: 'KUBECONFIG')]) {
                    sh """
                        kubectl apply -f k8s/namespace.yaml
                        kubectl apply -f k8s/configmap.yaml
                        kubectl apply -f k8s/secrets.yaml
                        kubectl apply -f k8s/postgres.yaml
                        kubectl apply -f k8s/kafka.yaml
                        kubectl apply -f k8s/keycloak.yaml
                        kubectl apply -f k8s/backend-deployment.yaml
                        kubectl apply -f k8s/notification-service-deployment.yaml
                        kubectl apply -f k8s/frontend-deployment.yaml
                        kubectl apply -f k8s/ingress.yaml
                        kubectl apply -f k8s/hpa.yaml

                        kubectl -n ${KUBE_NAMESPACE} set image deployment/backend backend=${BACKEND_IMAGE}:${IMAGE_TAG}
                        kubectl -n ${KUBE_NAMESPACE} set image deployment/frontend frontend=${FRONTEND_IMAGE}:${IMAGE_TAG}
                        kubectl -n ${KUBE_NAMESPACE} set image deployment/notification-service notification-service=${NOTIFY_IMAGE}:${IMAGE_TAG}

                        kubectl -n ${KUBE_NAMESPACE} rollout status deployment/backend --timeout=180s
                        kubectl -n ${KUBE_NAMESPACE} rollout status deployment/frontend --timeout=120s
                        kubectl -n ${KUBE_NAMESPACE} rollout status deployment/notification-service --timeout=120s
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Deployed build #${IMAGE_TAG} to ${KUBE_NAMESPACE}."
        }
        failure {
            echo "Pipeline failed — check the stage logs above. No rollback is automatic; " +
                 "run 'kubectl -n ${KUBE_NAMESPACE} rollout undo deployment/<name>' if needed."
        }
    }
}
