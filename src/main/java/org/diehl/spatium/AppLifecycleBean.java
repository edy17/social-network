package org.diehl.spatium;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.diehl.spatium.model.Organization;
import org.diehl.spatium.model.Post;
import org.diehl.spatium.model.User;
import org.diehl.spatium.repository.OrganizationRepository;
import org.diehl.spatium.repository.PostRepository;
import org.diehl.spatium.repository.UserRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class AppLifecycleBean {

    private static final Logger logger = LoggerFactory.getLogger("org.diehl.spatium.AppLifecycleBean");

    private static final List<String> dynamodbTableNames = Arrays.asList("Organization", "User", "Post");


    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    OrganizationRepository organizationRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    PostRepository postRepository;


    void onStart(@Observes StartupEvent event) {
        logger.info("The application is starting... {}", event);

        CompletableFuture<ListTablesResponse> response = dynamoDB.listTables(ListTablesRequest.builder().build());
        // Map the response to another CompletableFuture containing just the table names
        CompletableFuture<List<String>> tableNames = response.thenApply(ListTablesResponse::tableNames);
        // When future is complete (either successfully or in error) handle the response
        tableNames.whenComplete((tables, err) -> {
            if (tables != null) {
                AtomicBoolean dbIsClean = new AtomicBoolean(false);
                dynamodbTableNames.stream().forEach(dynamodbTableName -> {
                    if (tables.contains(dynamodbTableName)) {
                        logger.info("Table {} already exists", dynamodbTableName);
                    } else {
                        logger.info("Table {} does not exist", dynamodbTableName);
                        CreateTableRequest createTableRequest;
                        switch (dynamodbTableName) {
                            case "Organization":
                                createTableRequest = organizationRepository.createTableRequest();
                                dbIsClean.set(true);
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
                        CompletableFuture<CreateTableResponse> createTableCompletableFuture = dynamoDB.createTable(createTableRequest);
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
                if (dbIsClean.get()) {
                    logger.info("Generating data for dev... {}", dbIsClean);
                    try {
                        initDevDataBase();
                    } catch (Exception e) {
                        logger.error("An exception occurred!", e);
                    }
                }
            } else if (err != null) {
                logger.error("An exception occurred!", err);
            }
        });
        tableNames.join();
    }

    void onStop(@Observes ShutdownEvent event) {
        logger.info("The application is stopping... {}", event);
    }

    public void initDevDataBase() throws Exception {
        List<String> organizations = Arrays.asList("Facebook", "Dassault", "SNCF");
        byte[] array = new byte[10];
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("img/image.jpg");
        byte[] unknownImage = new byte[inputStream.available()];
        inputStream.read(unknownImage);
        organizations.stream().forEach(organizationName -> {
            try {
                dynamoDB.getItem(organizationRepository.getByIdRequest(organizationName))
                        .thenApply(response -> organizationRepository.getObject(response.item())).get();
            } catch (Exception e) {
                User u = new User();
                new Random().nextBytes(array);
                u.setUsername(new String(array, StandardCharsets.UTF_8));
                new Random().nextBytes(array);
                u.setPassword(new String(array, StandardCharsets.UTF_8));
                new Random().nextBytes(array);
                u.setEmail(new String(array, StandardCharsets.UTF_8));
                u.setId(UUID.randomUUID().toString());
                dynamoDB.putItem(userRepository.putRequest(u));
                logger.info("Creating new user... {}", u);
                Organization o = new Organization(organizationName);
                o.setUsers(Arrays.asList(u.getId()));
                o.setPosts(new ArrayList<>());
                for (int i = 0; i < 5; i++) {
                    Post p = new Post();
                    new Random().nextBytes(array);
                    p.setDescription(new String(array, StandardCharsets.UTF_8));
                    p.setImage(unknownImage);
                    p.setInstant(new DateTime(DateTimeZone.UTC));
                    p.setVisible(true);
                    p.setReportsNumber(0);
                    p.setOrganizationId(o.getName());
                    p.setUserId(u.getId());
                    dynamoDB.putItem(postRepository.putRequest(p));
                    logger.info("Creating new post... {}", p);
                    o.getPosts().add(p.getId());
                }
                dynamoDB.putItem(organizationRepository.putRequest(o));
                logger.info("Creating new organization... {}", o);
            }
        });

    }

}
