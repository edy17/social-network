package org.diehl.spatium.service;

import org.diehl.spatium.model.Comment;
import org.diehl.spatium.model.Post;
import org.diehl.spatium.repository.CommentRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class CommentService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    CommentRepository commentRepository;
    @Inject
    PostService postService;

    public CompletableFuture<List<Comment>> findByPublicPost(String postId) {
        Post post = postService.getByKeySchema(postId).join();
        if (post.isPublic()) {
            return dynamoDB.scan(commentRepository.scanByPostIdRequest(postId, null))
                    .thenApply(res -> {
                        Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                        Stream<Comment> commentStream = res.items().stream().map(commentRepository::getObject);
                        while (!lastKeyEvaluated.isEmpty()) {
                            AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Comment>> nextPage
                                    = this.getPage(commentRepository.scanByPostIdRequest(postId, lastKeyEvaluated)).join();
                            lastKeyEvaluated = nextPage.getKey();
                            commentStream = Stream.concat(commentStream, nextPage.getValue());
                        }
                        return commentStream.collect(Collectors.toList());
                    });
        }
        throw new NotAuthorizedException("Not authorized comment attempt");
    }

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Comment>>> getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<Comment> commentStream = response.items().stream().map(commentRepository::getObject);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), commentStream);
                });
    }

    public CompletableFuture<Comment> addPublicComment(Comment c) {
        c.setId(UUID.randomUUID().toString());
        c.setInstant(new Date());
        postService.addPublicComment(c.getPostId(), c.getId()).join();
        return dynamoDB.putItem(commentRepository.putRequest(c))
                .thenApply(response -> commentRepository.getObject(response.attributes()));
    }
}
