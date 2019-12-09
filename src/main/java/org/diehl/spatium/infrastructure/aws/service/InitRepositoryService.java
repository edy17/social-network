package org.diehl.spatium.infrastructure.aws.service;

import org.diehl.spatium.infrastructure.aws.dynamodb.CommentDynamoDbRepository;
import org.diehl.spatium.infrastructure.aws.dynamodb.OrganizationDynamoDbRepository;
import org.diehl.spatium.infrastructure.aws.dynamodb.PostDynamoDbRepository;
import org.diehl.spatium.infrastructure.aws.dynamodb.UserDynamoDbRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class InitRepositoryService {

    private static final List<String> dynamodbTableNames = Arrays.asList("Organization", "User", "Post", "Comment");

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    PostDynamoDbRepository postDynamoDbRepository;
    @Inject
    UserDynamoDbRepository userDynamoDbRepository;
    @Inject
    OrganizationDynamoDbRepository organizationDynamoDbRepository;
    @Inject
    CommentDynamoDbRepository commentDynamoDbRepository;

    public CreateTableResponse createTable(String dynamodbTableName) {
        CreateTableRequest createTableRequest;
        switch (dynamodbTableName) {
            case "Organization":
                createTableRequest = organizationDynamoDbRepository.createTableRequest();
                break;
            case "User":
                createTableRequest = userDynamoDbRepository.createTableRequest();
                break;
            case "Post":
                createTableRequest = postDynamoDbRepository.createTableRequest();
                break;
            case "Comment":
                createTableRequest = commentDynamoDbRepository.createTableRequest();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dynamodbTableName);
        }
        CreateTableResponse createTableResponse = dynamoDB.createTable(createTableRequest).join();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            // Restore interrupted state...      
            Thread.currentThread().interrupt();
        }
        return createTableResponse;
    }

    public List<String> getTableNames() {
        CompletableFuture<ListTablesResponse> response = dynamoDB.listTables(ListTablesRequest.builder().build());
        return response.thenApply(ListTablesResponse::tableNames).join();
    }

    public static List<String> getDynamodbTableNames() {
        return dynamodbTableNames;
    }
}
