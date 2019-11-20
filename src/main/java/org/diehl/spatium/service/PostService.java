package org.diehl.spatium.service;

import org.diehl.spatium.model.Post;
import org.diehl.spatium.repository.PostRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@ApplicationScoped
public class PostService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    PostRepository postRepository;

    public CompletableFuture<List<Post>> findAll() {
        return dynamoDB.scan(postRepository.scanRequest())
                .thenApply(res -> res.items().stream().map(Post::from).collect(Collectors.toList()));
    }

    public CompletableFuture<List<Post>> add(Post post) {
        return dynamoDB.putItem(postRepository.putRequest(post)).thenCompose(ret -> findAll());
    }

    public CompletableFuture<Post> getById(String id) {
        return dynamoDB.getItem(postRepository.getByIdRequest(id)).thenApply(response -> Post.from(response.item()));
    }
}
