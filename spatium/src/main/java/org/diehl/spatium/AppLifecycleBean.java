package org.diehl.spatium;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.diehl.spatium.application.SpatiumAPI;
import org.diehl.spatium.domain.model.Comment;
import org.diehl.spatium.domain.model.Organization;
import org.diehl.spatium.domain.model.Post;
import org.diehl.spatium.domain.model.User;
import org.diehl.spatium.infrastructure.aws.service.InitRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class AppLifecycleBean {

    private static final Logger logger = LoggerFactory.getLogger(AppLifecycleBean.class);

    @Inject
    InitRepositoryService initRepositoryService;
    @Inject
    SpatiumAPI spatiumAPI;

    void onStart(@Observes StartupEvent event) {
        logger.info("The application is starting... {}", event);
        List<String> existingTableNames = initRepositoryService.getTableNames();
        AtomicBoolean dbIsClean = new AtomicBoolean(false);
        InitRepositoryService.getDynamodbTableNames().forEach(dynamodbTableName -> {
            if (existingTableNames.contains(dynamodbTableName)) {
                logger.info("Table {} already exists", dynamodbTableName);
            } else {
                logger.info("Table {} does not exist", dynamodbTableName);
                String table = initRepositoryService.createTable(dynamodbTableName).tableDescription().tableName();
                logger.info("Table {} was created", table);
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

    private void initDevDataBase() {

        byte[] randomGenerator = new byte[10];
        User user = createRandomUser(randomGenerator);
        Arrays.asList("CFA_INSTA", "Facebook", "Dassault", "SNCF").forEach(organizationName -> {
            Organization organization = new Organization();
            organization.setName(organizationName);
            organization.setUserIdsOfMembers(Collections.singletonList(user.getId()));
            Organization tmp = spatiumAPI.addOrganization(organization).join();
            if (tmp != null) {
                logger.info("Creating new organization done... {}", tmp);
                createRandomPosts(randomGenerator, user, organization);
            }
        });
    }

    private User createRandomUser(byte[] randomGenerator) {
        User u = new User();
        new Random().nextBytes(randomGenerator);
        u.setUsername(new String(randomGenerator, StandardCharsets.UTF_8));
        new Random().nextBytes(randomGenerator);
        u.setPassword(new String(randomGenerator, StandardCharsets.UTF_8));
        new Random().nextBytes(randomGenerator);
        u.setEmail(new String(randomGenerator, StandardCharsets.UTF_8));
        spatiumAPI.addUser(u).join();
        logger.info("Creating new user... {}", u);
        return u;
    }

    private void createRandomPosts(byte[] randomGenerator, User u, Organization organization) {
        for (int i = 0; i < 2; i++) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("img/image.jpg");
            logger.info("Unknown Image was read... ");
            Post p = new Post();
            new Random().nextBytes(randomGenerator);
            p.setDescription(new String(randomGenerator, StandardCharsets.UTF_8));
            p.setImage(inputStream);
            p.setReportsNumber(0);
            p.setUserId(u.getId());
            p.setOrganizationId(organization.getName());
            spatiumAPI.addPost(p).join();
            logger.info("Creating new post done... {}", p);
            createRandomComments(randomGenerator, u, p);
        }
    }

    private void createRandomComments(byte[] randomGenerator, User u, Post p) {
        for (int i = 0; i < 2; i++) {
            Comment c = new Comment();
            new Random().nextBytes(randomGenerator);
            c.setContent(new String(randomGenerator, StandardCharsets.UTF_8));
            c.setUserId(u.getId());
            c.setPostId(p.getId());
            spatiumAPI.addComment(c).join();
            logger.info("Creating new comment done... {}", c);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                // Restore interrupted state...      
                Thread.currentThread().interrupt();
            }
        }
    }
}
