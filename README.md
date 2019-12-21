# SPATIUM Application, version 1.0-SNAPSHOT
Application based on a native cloud serverless architecture.
The version 1.0-SNAPSHOT offers
- Implementation and deployment model on AWS lambda or Kubernetes. The application communicates with AWS DynamoDB and S3 services.
- Approach of Domain-driven design
- Java Asynchronous programming 


## To run Development server with AWS Lambda, DynamoDB and S3

- Create file `spatium/src/main/resources/application.properties`, and add there:
```
quarkus.http.cors=true
quarkus.dynamodb.aws.region=<AWS Region>
quarkus.dynamodb.aws.credentials.type=static
quarkus.dynamodb.aws.credentials.static-provider.access-key-id=<AWS access key ID>
quarkus.dynamodb.aws.credentials.static-provider.secret-access-key=<AWS Secret access key>
spatium.image.bucket.name=<Existing S3 bucket in the specified region for save image>
```
- Make sure you have installed [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), [Maven](https://maven.apache.org/install.html), [AWS-CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv1.html), [AWS-SAM](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html), and with your  AWS credentials.
- To run local Development server, listening port `5005` for remote debugging, go to `spatium` subfolder and run
```
mvn clean install
mvn compile quarkus:dev -Ddebug
```
- To Run Server on JVM Serveless Container, go to `spatium` subfolder and run
```
mvn clean install
aws cloudformation package --template-file sam.jvm.yaml --output-template-file output-sam.yaml --s3-bucket <Existing S3 bucket in the specified region for save dev builds>
aws cloudformation deploy --template-file output-sam.yaml --stack-name SpatiumServerless --capabilities CAPABILITY_IAM --region <AWS Region> 
```
- To Run Server on provided Lambda Linux Runtime
 - Make sure you have installed Docker
 - In `spatium` subfolder, Compile source in native image executable with GraalVm docker container running
```
docker-compose up
```
 - Then run
```
aws cloudformation package --template-file sam.native.yaml --output-template-file output-sam.yaml --s3-bucket <Existing S3 bucket in the specified region for save dev builds>
aws cloudformation deploy --template-file output-sam.yaml --stack-name SpatiumServerless --capabilities CAPABILITY_IAM --region <AWS Region>
```
 
 
## To run local web front

- Make sure you have installed NodeJS
- set a Spatium api url in `view/src/environments/environment.ts`
 - Default value is `http://localhost:8080` knowing that you run local dev server with `mvn compile quarkus:dev -Ddebug`
 - If you run dev server on aws, you cant get result Api url on AWS CloudFormation task description
- In `view` folder, Run `npm install` and `ng serve` for a dev server. Navigate to `http://localhost:4200/`.


## To deploy production environment on Kubernetes and CI/CD pipeline with Jenkins

- Create a Kubernetes Cluster at your cloud provider, and get a `kubeconfig` file
- Make sure you have installed locally [Kubectl](https://kubernetes.io/fr/docs/tasks/tools/install-kubectl/) and store `kubeconfig` file
- Connect a docker registry to a cluster to host applications
```
kubectl create secret docker-registry regcred --docker-server="<DOCEKR_REGISTRY>" --docker-username="<DOCEKR_USERNAME>" --docker-password="<DOCEKR_PASSWORD>" --docker-email="<DOCEKR_EMAIL>"
```
- Make sure you have installed [Helm](https://helm.sh/docs/intro/install/) on your Kubernetes cluster with Kubectl
- Create a Kubernetes deployment of Jenkins named `kissing-giraffe-jenkins`  with helm:
```
helm install --name kissing-giraffe-jenkins --set master.servicetype=NodePort stable/jenkins
```
- Then follow:
- Get your 'admin' user password by running:
```
printf $(kubectl get secret --namespace default kissing-giraffe-jenkins -o jsonpath="{.data.jenkins-admin-password}" | base64 --decode);echo
```
- Get the Jenkins URL to visit by running these commands in the same shell:
      NOTE: It may take a few minutes for the LoadBalancer IP to be available.
            You can watch the status of by running `kubectl get svc --namespace default -w kissing-giraffe-jenkins`
 ```
export SERVICE_IP=$(kubectl get svc --namespace default kissing-giraffe-jenkins --template "{{ range (index .status.loadBalancer.ingress 0) }}{{ . }}{{ end }}")
echo http://$SERVICE_IP:8080/login
```
- Login with the password from step 1 and the username: `admin`
- Save all needed environment variables for Jenkinsfile
    - Jenkins global properties: `AWS_REGION`, `AWS_IMAGE_BUCKET` and `DOCKER_REGISTRY`
    - Jenkins credentials: `AWS_CREDENTIALS_ID`, `DOCKER_CREDENTIALS_ID` and provide `K8s_CREDENTIALS_ID`
- Set image of Docker and Kubectl as Jenkins agent in Kubernetes Pod template configuration
- In `k8s/jenkins` folder, Run `/init-jenkins.sh` to authorize Jenkins to create Kubernetes Object
- Run pipeline
