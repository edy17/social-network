package org.diehl.spatium.service;

import org.diehl.spatium.repository.OrganizationRepository;
import org.diehl.spatium.repository.PostRepository;
import org.diehl.spatium.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class InitSpatiumService {

    private static final Logger logger = LoggerFactory.getLogger("org.diehl.spatium.service.InitSpatiumTables");

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    PostRepository postRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    OrganizationRepository organizationRepository;

    public void createTable(String dynamodbTableName) throws InterruptedException {
        CreateTableRequest createTableRequest;
        switch (dynamodbTableName) {
            case "Organization":
                createTableRequest = organizationRepository.createTableRequest();
                break;
            case "User":
                createTableRequest = userRepository.createTableRequest();
                break;
            case "Post":
                createTableRequest = postRepository.createTableRequest();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dynamodbTableName);
        }
        CompletableFuture<CreateTableResponse> createTableCompletableFuture = dynamoDB.createTable(createTableRequest)
                .whenComplete((createTableResponse, err)-> {
                    if (createTableResponse != null) {
                        String createTableNames = createTableResponse.tableDescription().tableName();
                        logger.info("Table {} was created", createTableNames);
                    } else if (err != null){
                        logger.error("An error occurred when creating table!", err);
                    }
                });
        createTableCompletableFuture.join();
        TimeUnit.SECONDS.sleep(5);
    }

    public List<String> getTableNames() throws ExecutionException, InterruptedException {
        CompletableFuture<ListTablesResponse> response = dynamoDB.listTables(ListTablesRequest.builder().build());
        CompletableFuture<List<String>> completableFuture = response.thenApply(ListTablesResponse::tableNames);
        completableFuture.whenComplete((tables, err) -> {
            if ((err != null)||(tables == null)) {
                logger.error("An error occurred!", err);
                throw new IllegalStateException("Error when list Tables");
            }
        });
        completableFuture.join();
        return completableFuture.get();
    }
}
