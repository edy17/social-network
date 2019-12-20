# SPATIUM Application, version 1.0-SNAPSHOT


## To run Development server with AWS Lambda, DynamoDB and S3
- Create file `src/main/resources/application.properties`, and add there:
```
quarkus.http.cors=true
quarkus.dynamodb.aws.region=<AWS Region>
quarkus.dynamodb.aws.credentials.type=static
quarkus.dynamodb.aws.credentials.static-provider.access-key-id=<AWS access key ID>
quarkus.dynamodb.aws.credentials.static-provider.secret-access-key=<AWS Secret access key>
spatium.image.bucket.name=<Existing S3 bucket in the specified region for save image>
```
- Make sure you have installed Java8, Maven, AWS-CLI and AWS-SAM

-To run local Development server, listening port `5005` for remote debugging
```
mvn clean install
mvn compile quarkus:dev -Ddebug
```

- To Run Server on JVM Serveless Container
```
mvn clean install
aws cloudformation package --template-file sam.jvm.yaml --output-template-file output-sam.yaml --s3-bucket <Existing S3 bucket in the specified region for save dev builds>
aws cloudformation deploy --template-file output-sam.yaml --stack-name SpatiumServerless --capabilities CAPABILITY_IAM --region <AWS Region> 
```
- To Run Server on provided Lanbda Linux Runtime
    - Make sure you have installed Docker
    - Compile source on native image executable with GraalVm docker container
    ```
        docker-compose up
    ```
    - Run
    ```
  aws cloudformation package --template-file sam.native.yaml --output-template-file output-sam.yaml --s3-bucket <Existing S3 bucket in the specified region for save dev builds>
  aws cloudformation deploy --template-file output-sam.yaml --stack-name SpatiumServerless --capabilities CAPABILITY_IAM --region <AWS Region>
    ```
 
## To run local web front
- Make sure you have installed NodeJS
- set a Spatium api url in `view/src/environments/environment.ts`
 - Default value is `http://localhost:8080` knowing that you run server local dev server with `mvn compile quarkus:dev -Ddebug`
 - If you run dev server, on aws, you cant get result Api url, AWS CloudFormation task description
- In `view` folder, Run `npm install` and `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## To run Pipeline that performs Production server on AWS Lambda, DynamoDB, S3 then Web distribution on AWS cloudFront
- Configure Jenkins Server and save all needed parameter as global properties
- Improve Jenkins File, push to repository and run pipeline
