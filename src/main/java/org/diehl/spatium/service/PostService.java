package org.diehl.spatium.service;

import org.diehl.spatium.model.Organization;
import org.diehl.spatium.model.Post;
import org.diehl.spatium.repository.PostRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@ApplicationScoped
public class PostService {

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

    public CompletableFuture<Post> add(Post post) {
        Organization organization = organizationService.getById(post.getId()).getNow(new Organization());
        if(organization.getName() !=null) {
            post.setId(UUID.randomUUID().toString());
            post.setInstant(new DateTime(DateTimeZone.UTC));
            if(organization.getPosts() == null) {
                organization.setPosts(new ArrayList<String>());
            }
            organization.getPosts().add(post.getId());
        }
        organizationService.update(organization);
        return dynamoDB.putItem(postRepository.putRequest(post)).thenApply(response -> postRepository.getObject(response.attributes()));
    }
}
