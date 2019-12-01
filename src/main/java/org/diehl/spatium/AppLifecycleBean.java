package org.diehl.spatium;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.diehl.spatium.repository.OrganizationRepository;
import org.diehl.spatium.repository.PostRepository;
import org.diehl.spatium.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class AppLifecycleBean {

    private static final Logger logger = LoggerFactory.getLogger("org.diehl.spatium.AppLifecycleBean");

    private static final List<String> dynamodbTableNames = Arrays.asList("Organization", "User", "Post");


    @Inject
    DynamoDbAsyncClient client;
    @Inject
    OrganizationRepository organizationRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    PostRepository postRepository;


    void onStart(@Observes StartupEvent event) {
        logger.info("The application is starting... {}", event);
        CompletableFuture<ListTablesResponse> response = client.listTables(ListTablesRequest.builder().build());
        // Map the response to another CompletableFuture containing just the table names
        CompletableFuture<List<String>> tableNames = response.thenApply(ListTablesResponse::tableNames);
        // When future is complete (either successfully or in error) handle the response
        tableNames.whenComplete((tables, err) -> {
            if (tables != null) {
                dynamodbTableNames.stream().forEach(dynamodbTableName -> {
                    if (tables.contains(dynamodbTableName)) {
                        logger.info("Table {} already exists", dynamodbTableName);
                    } else {
                        logger.info("Table {} does not exist", dynamodbTableName);
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
                        CompletableFuture<CreateTableResponse> createTableCompletableFuture = client.createTable(createTableRequest);
                        try {
                            CreateTableResponse createTableResponse = createTableCompletableFuture.get();
                            if (createTableResponse != null) {
                                String createTableNames = createTableResponse.tableDescription().tableName();
                                logger.info("Table {} was created", createTableNames);
                            }
                        } catch (Exception e) {
                            logger.error("An exception occurred!", e);
                        }
                    }
                });
            } else if (err != null) {
                logger.error("An exception occurred!", err);
            }
        });
        tableNames.join();
    }

    void onStop(@Observes ShutdownEvent event) {
        logger.info("The application is stopping... {}", event);
    }

}
