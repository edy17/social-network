package org.diehl.spatium.service;

import org.diehl.spatium.model.Organization;
import org.diehl.spatium.model.Post;
import org.diehl.spatium.repository.PostRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger("org.diehl.spatium.service.PostService");


    private static final int IMG_WIDTH = 280;
    private static final int IMG_HEIGHT = 350;

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    PostRepository postRepository;
    @Inject
    OrganizationService organizationService;

    public CompletableFuture<List<Post>> findAll() {
        return dynamoDB.scan(postRepository.scanRequest())
                .thenApply(res -> res.items().stream().map(postRepository::getObject).collect(Collectors.toList()));
    }

    public CompletableFuture<Post> getById(String id) {
        return dynamoDB.getItem(postRepository.getByIdRequest(id))
                .thenApply(response -> postRepository.getObject(response.item()));
    }

    public CompletableFuture<Post> add(Post post) throws IOException, ExecutionException, InterruptedException {
        Organization organization = organizationService.getById(post.getOrganizationId()).join();
        post.setId(UUID.randomUUID().toString());
        post.setInstant(new DateTime(DateTimeZone.UTC));
        InputStream in = new ByteArrayInputStream(post.getImage());
        BufferedImage originalImage = ImageIO.read(in);
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        BufferedImage resizeImage = resizeImage(originalImage, type);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizeImage, "png", baos);
        baos.flush();
        post.setImage(baos.toByteArray());
        baos.close();
        List<String> posts = new ArrayList<>();
        if(organization.getPosts()!=null) {
            posts.addAll(organization.getPosts());
        }
        organization.setPosts(posts);
        organizationService.update(organization).join();
        return dynamoDB.putItem(postRepository.putRequest(post)).thenApply(response -> postRepository.getObject(response.attributes()));
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int type) {
        BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();
        return resizedImage;
    }
}
