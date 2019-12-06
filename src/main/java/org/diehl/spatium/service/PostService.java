package org.diehl.spatium.service;

import org.diehl.spatium.model.Post;
import org.diehl.spatium.repository.PostRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class PostService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    PostRepository postRepository;


    public CompletableFuture<List<Post>> findByPublic() {
        return dynamoDB.scan(postRepository.scanByPublicRequest(null))
                .thenApply(res -> {
                    Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                    Stream<Post> postStream = res.items().stream().map(postRepository::getObject);
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Post>> nextPage
                                = this.getPage(postRepository.scanByPublicRequest(lastKeyEvaluated)).join();
                        lastKeyEvaluated = nextPage.getKey();
                        postStream = Stream.concat(postStream, nextPage.getValue());
                    }
                    return postStream.collect(Collectors.toList());
                });
    }

    public CompletableFuture<List<Post>> findByOrganization(String organizationId) {
        return dynamoDB.scan(postRepository.scanByOrganization(organizationId, null))
                .thenApply(response -> {
                    Stream<Post> postStream = response.items().stream().map(postRepository::getObject);
                    Map<String, AttributeValue> lastKeyEvaluated = response.lastEvaluatedKey();
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Post>> nextPage
                                = this.getPage(postRepository.scanByOrganization(organizationId, lastKeyEvaluated)).join();
                        lastKeyEvaluated = nextPage.getKey();
                        postStream = Stream.concat(postStream, nextPage.getValue());
                    }
                    return postStream.collect(Collectors.toList());
                });
    }

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Post>>> getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<Post> postStream = response.items().stream().map(postRepository::getObject);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), postStream);
                });
    }

    public CompletableFuture<Post> getByKeySchema(String keySchema) {
        return dynamoDB.getItem(postRepository.getByKeySchemaRequest(keySchema))
                .thenApply(response -> postRepository.getObject(response.item()));
    }

    public CompletableFuture<Post> addPublicPost(Post p) {
        p.setId(UUID.randomUUID().toString());
        p.setInstant(new Date());
        p.setPublic(true);
        return dynamoDB.putItem(postRepository.putRequest(p))
                .thenApply(response -> postRepository.getObject(response.attributes()));
    }

    public CompletableFuture<Post> addPublicComment(String postId, String commentId) {
        Post post = getByKeySchema(postId).join();
        if (post.isPublic()) {
            ArrayList<String> commentIds = new ArrayList<>(post.getCommentIds());
            commentIds.add(commentId);
            post.setCommentIds(commentIds);
            return dynamoDB.updateItem(postRepository.addCommentRequest(post)).thenApply(response -> postRepository.getObject(response.attributes()));
        }
        throw new NotAuthorizedException("Not authorized comment attempt");
    }

}
