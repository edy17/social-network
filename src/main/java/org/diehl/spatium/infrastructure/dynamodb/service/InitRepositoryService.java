package org.diehl.spatium.infrastructure.dynamodb.service;

import org.diehl.spatium.infrastructure.dynamodb.repository.CommentRepository;
import org.diehl.spatium.infrastructure.dynamodb.repository.OrganizationRepository;
import org.diehl.spatium.infrastructure.dynamodb.repository.PostRepository;
import org.diehl.spatium.infrastructure.dynamodb.repository.UserRepository;
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
    PostRepository postRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    OrganizationRepository organizationRepository;
    @Inject
    CommentRepository commentRepository;

    public CreateTableResponse createTable(String dynamodbTableName) {
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
            case "Comment":
                createTableRequest = commentRepository.createTableRequest();
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
