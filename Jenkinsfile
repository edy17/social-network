def url = "api-service"
node('kissing-giraffe-jenkins-slave') {
    withEnv(["api_image_tag=${env.DOCKER_REGISTRY}/${env.DOCKER_REPOSITORY}/api:web-v${env.BUILD_NUMBER}",
             "client_image_tag=${env.DOCKER_REGISTRY}/${env.DOCKER_REPOSITORY}/client:web-v${env.BUILD_NUMBER}"]) {
        stage('Checkout') {
            checkout scm
        }
        stage('Get API URL') {
            container('kubectl') {
                withKubeConfig([credentialsId: env.K8s_CREDENTIALS_ID,
                                serverUrl    : env.K8s_SERVER_URL,
                                contextName  : env.K8s_CONTEXT_NAME,
                                clusterName  : env.K8s_CLUSTER_NAME]) {
                    url = sh(
                            script: 'kubectl describe service api-service | grep Ingress | awk \'{print \$(NF)}\'',
                            returnStdout: true
                    ).trim()
                }
            }
        }
        stage('Build and Push Docker Image to Registry') {
            container('docker') {
                withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                                  credentialsId   : env.DOCKER_CREDENTIALS_ID,
                                  usernameVariable: 'USERNAME',
                                  passwordVariable: 'PASSWORD']]) {
                    docker.withRegistry("https://${env.DOCKER_REGISTRY}", env.DOCKER_CREDENTIALS_ID) {
                        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                                          credentialsId   : env.AWS_CREDENTIALS_ID,
                                          usernameVariable: 'AWS_ACCESS_KEY_ID',
                                          passwordVariable: 'AWS_ACCESS_KEY_SECRET']]) {
                            dir("spatium") {
                                sh """
                                    docker build --build-arg AWS_REGION=${env.AWS_REGION} \
                                    --build-arg AWS_IMAGE_BUCKET=${env.AWS_IMAGE_BUCKET} \
                                    --build-arg AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
                                    --build-arg AWS_ACCESS_KEY_SECRET=$AWS_ACCESS_KEY_SECRET \
                                    -f Dockerfile.jvm \
                                    -t ${api_image_tag} .
                                    docker push ${api_image_tag} 
                                    docker rmi -f ${api_image_tag}                              
                                """
                            }
                            dir("view") {
                                sh """
                                    docker build --build-arg BACKEND_API_URL=http://${url} -t ${client_image_tag} .
                                    docker push ${client_image_tag} 
                                    docker rmi -f ${client_image_tag}                                   
                                """
                            }
                        }
                    }
                }
            }
        }
        stage('Deploy on k8s') {
            container('kubectl') {
                withKubeConfig([credentialsId: env.K8s_CREDENTIALS_ID,
                                serverUrl    : env.K8s_SERVER_URL,
                                contextName  : env.K8s_CONTEXT_NAME,
                                clusterName  : env.K8s_CLUSTER_NAME]) {
                    if (env.BUILD_NUMBER == '1') {
                        dir("k8s") {
                            sh """
                            envsubst '$api_image_tag' < api-deployment.yaml > api.tmp
                            mv api.tmp api-deployment.yaml
                            envsubst '$client_image_tag' < client-deployment.yaml > client.tmp
                            mv client.tmp client-deployment.yaml
                            kubectl create -f api-service.yaml
                            kubectl create -f client-service.yaml
                            kubectl create -f api-deployment.yaml
                            kubectl create -f client-deployment.yaml
                           """
                        }
                    } else {
                        sh """
                        kubectl set image deployment/api-deployment spatium-backend=${api_image_tag}
                        kubectl rollout status -w deployment/api-deployment
                        kubectl set image deployment/client-deployment spatium-frontend=${client_image_tag}
                        kubectl rollout status -w deployment/client-deployment  
                    """
                    }
                }
            }
        }
    }
}



