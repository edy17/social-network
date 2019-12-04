package org.diehl.spatium;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.diehl.spatium.model.Organization;
import org.diehl.spatium.model.Post;
import org.diehl.spatium.model.User;
import org.diehl.spatium.service.InitSpatiumService;
import org.diehl.spatium.service.OrganizationService;
import org.diehl.spatium.service.PostService;
import org.diehl.spatium.service.UserService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
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
    InitSpatiumService initSpatiumService;
    @Inject
    PostService postService;
    @Inject
    UserService userService;
    @Inject
    OrganizationService organizationService;


    void onStart(@Observes StartupEvent event) throws ExecutionException, InterruptedException, IOException {
        logger.info("The application is starting... {}", event);
        List<String> tableNames = initSpatiumService.getTableNames();
        AtomicBoolean dbIsClean = new AtomicBoolean(false);
        dynamodbTableNames.stream().forEach(dynamodbTableName -> {
            if (tableNames.contains(dynamodbTableName)) {
                logger.info("Table {} already exists", dynamodbTableName);
            } else {
                logger.info("Table {} does not exist", dynamodbTableName);
                try {
                    initSpatiumService.createTable(dynamodbTableName);
                } catch (InterruptedException e) {
                    logger.error("An exception occurred... ", e);
                    // Restore interrupted state...      
                    Thread.currentThread().interrupt();
                }
                if (dynamodbTableName.equals("Organization")) {
                    dbIsClean.set(true);
                }
            }
        });
        if (dbIsClean.get()) {
            logger.info("Generating data for dev... {}", dbIsClean);
            initDevDataBase();
        }
    }

    void onStop(@Observes ShutdownEvent event) {
        logger.info("The application is stopping... {}", event);
    }

    private void initDevDataBase() throws IOException {

        byte[] array = new byte[10];
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("img/image.jpg");
        byte[] unknownImage = new byte[0];
        if (inputStream != null) {
            unknownImage = new byte[inputStream.available()];
            int bytes = inputStream.read(unknownImage);
            logger.info("Unknown Image was read in {} bytes... ", bytes);
        }
        byte[] finalUnknownImage = unknownImage;

        List<String> organizations = Arrays.asList("Facebook", "Dassault", "SNCF");
        organizations.forEach(organizationName -> {
                    User u = new User();
                    new Random().nextBytes(array);
                    u.setUsername(new String(array, StandardCharsets.UTF_8));
                    new Random().nextBytes(array);
                    u.setPassword(new String(array, StandardCharsets.UTF_8));
                    new Random().nextBytes(array);
                    u.setEmail(new String(array, StandardCharsets.UTF_8));
                    u.setId(UUID.randomUUID().toString());
                    User user = userService.add(u).join();
                    if (user != null) {
                        logger.info("Creating new user... {}", user);
                    }
                    Organization o = new Organization();
                    o.setName(organizationName);
                    o.setUsers(Collections.singletonList(u.getId()));
                    Organization org = organizationService.add(o).join();
                    if (org != null) {
                        logger.info("Creating new organization done... {}", org);
                    }
                    for (int i = 0; i < 5; i++) {
                        Post p = new Post();
                        new Random().nextBytes(array);
                        p.setId(UUID.randomUUID().toString());
                        p.setDescription(new String(array, StandardCharsets.UTF_8));
                        p.setImage(finalUnknownImage);
                        p.setInstant(new DateTime(DateTimeZone.UTC));
                        p.setVisible(true);
                        p.setReportsNumber(0);
                        p.setOrganizationId(o.getName());
                        p.setUserId(u.getId());
                        Post post = null;
                        try {
                            post = postService.add(p).join();
                        } catch (Exception ex) {
                            logger.error("An exception occurred!", ex);
                        }
                        if (post != null) {
                            logger.info("Creating new post done... {}", p);
                        }
                    }
                }
        );
    }
}
