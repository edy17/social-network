package org.diehl.spatium.infrastructure.aws.service;

import org.diehl.spatium.domain.model.Post;
import org.diehl.spatium.domain.service.PostService;
import org.diehl.spatium.infrastructure.aws.dynamodb.PostDynamoDbRepository;
import org.diehl.spatium.infrastructure.aws.mapper.PostDynamoDbMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class PostAwsService implements PostService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    PostDynamoDbRepository postDynamoDbRepository;
    @Inject
    PostDynamoDbMapper postDynamoDbMapper;
    @Inject
    S3AwsService s3AwsService;

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Post>>>
    getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<Post> postStream = response.items().stream().map(postDynamoDbMapper::toPost);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), postStream);
                });
    }

    @Override
    public CompletableFuture<Post> findById(String id) {
        return dynamoDB.getItem(postDynamoDbRepository.getByKeySchemaRequest(id))
                .thenApply(response -> postDynamoDbMapper.toPost(response.item()));
    }

    @Override
    public CompletableFuture<List<Post>> findByPublic() {
        return dynamoDB.scan(postDynamoDbRepository.scanByPublicRequest(null))
                .thenApply(res -> {
                    Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                    Stream<Post> postStream = res.items().stream().map(postDynamoDbMapper::toPost);
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Post>> nextPage
                                = this.getPage(postDynamoDbRepository.scanByPublicRequest(lastKeyEvaluated)).join();
                        lastKeyEvaluated = nextPage.getKey();
                        postStream = Stream.concat(postStream, nextPage.getValue());
                    }
                    return postStream.collect(Collectors.toList());
                });
    }

    @Override
    public CompletableFuture<List<Post>> findByOrganization(String organizationId) {
        return dynamoDB.scan(postDynamoDbRepository.scanByOrganization(organizationId, null))
                .thenApply(response -> {
                    Stream<Post> postStream = response.items().stream().map(postDynamoDbMapper::toPost);
                    Map<String, AttributeValue> lastKeyEvaluated = response.lastEvaluatedKey();
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Post>> nextPage
                                = this.getPage(postDynamoDbRepository.scanByOrganization(organizationId, lastKeyEvaluated)).join();
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
        post.setImageKey(UUID.randomUUID().toString());
        s3AwsService.addImage(post.getImageKey(), post.getImage());
        return dynamoDB.putItem(postDynamoDbRepository.putRequest(postDynamoDbMapper.toDynamoDbItem(post)))
                .thenApply(response -> postDynamoDbMapper.toPost(response.attributes()));
    }

    @Override
    public InputStream getImage(String key) {
        return s3AwsService.getImage(key);
    }
}
