package org.diehl.spatium.infrastructure.dynamodb.service;

import org.diehl.spatium.domain.model.Comment;
import org.diehl.spatium.domain.model.Post;
import org.diehl.spatium.domain.service.CommentService;
import org.diehl.spatium.infrastructure.dynamodb.mapper.CommentDynamoMapper;
import org.diehl.spatium.infrastructure.dynamodb.repository.CommentRepository;
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
public class CommentDynamoService implements CommentService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    CommentRepository commentRepository;
    @Inject
    CommentDynamoMapper commentDynamoMapper;
    @Inject
    PostDynamoService postDynamoService;

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Comment>>>
    getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<Comment> commentStream = response.items().stream().map(commentDynamoMapper::toComment);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), commentStream);
                });
    }

    @Override
    public CompletableFuture<List<Comment>> findByPostId(String postId) {
        Post post = postDynamoService.findById(postId).join();
        if (post.isPublic()) {
            return dynamoDB.scan(commentRepository.scanByPostIdRequest(postId, null))
                    .thenApply(res -> {
                        Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                        Stream<Comment> commentStream = res.items().stream().map(commentDynamoMapper::toComment);
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

    @Override
    public CompletableFuture<Comment> add(Comment comment) {
        comment.setId(UUID.randomUUID().toString());
        comment.setInstant(new Date());
        return dynamoDB.putItem(commentRepository.putRequest(commentDynamoMapper.toDynamoDbItem(comment)))
                .thenApply(response -> commentDynamoMapper.toComment(response.attributes()));
    }
}
