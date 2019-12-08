package org.diehl.spatium.infrastructure.dynamodb.service;

import org.diehl.spatium.domain.model.Post;
import org.diehl.spatium.domain.service.PostService;
import org.diehl.spatium.infrastructure.dynamodb.mapper.PostDynamoMapper;
import org.diehl.spatium.infrastructure.dynamodb.repository.PostRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class PostDynamoService implements PostService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    PostRepository postRepository;
    @Inject
    PostDynamoMapper postDynamoMapper;

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Post>>>
    getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<Post> postStream = response.items().stream().map(postDynamoMapper::toPost);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), postStream);
                });
    }

    @Override
    public CompletableFuture<Post> findById(String id) {
        return dynamoDB.getItem(postRepository.getByKeySchemaRequest(id))
                .thenApply(response -> postDynamoMapper.toPost(response.item()));
    }

    @Override
    public CompletableFuture<List<Post>> findByPublic() {
        return dynamoDB.scan(postRepository.scanByPublicRequest(null))
                .thenApply(res -> {
                    Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                    Stream<Post> postStream = res.items().stream().map(postDynamoMapper::toPost);
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Post>> nextPage
                                = this.getPage(postRepository.scanByPublicRequest(lastKeyEvaluated)).join();
                        lastKeyEvaluated = nextPage.getKey();
                        postStream = Stream.concat(postStream, nextPage.getValue());
                    }
                    return postStream.collect(Collectors.toList());
                });
    }

    @Override
    public CompletableFuture<List<Post>> findByOrganization(String organizationId) {
        return dynamoDB.scan(postRepository.scanByOrganization(organizationId, null))
                .thenApply(response -> {
                    Stream<Post> postStream = response.items().stream().map(postDynamoMapper::toPost);
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

    @Override
    public CompletableFuture<Post> add(Post post) {
        post.setId(UUID.randomUUID().toString());
        post.setInstant(new Date());
        post.setPublic(true);
        return dynamoDB.putItem(postRepository.putRequest(postDynamoMapper.toDynamoDbItem(post)))
                .thenApply(response -> postDynamoMapper.toPost(response.attributes()));
    }
}
