package org.diehl.spatium.infrastructure.aws.service;

import org.diehl.spatium.domain.model.User;
import org.diehl.spatium.domain.service.UserService;
import org.diehl.spatium.infrastructure.aws.mapper.UserDynamoDbMapper;
import org.diehl.spatium.infrastructure.aws.dynamodb.UserDynamoDbRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class UserAwsService implements UserService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    UserDynamoDbRepository userDynamoDbRepository;
    @Inject
    UserDynamoDbMapper userDynamoDbMapper;

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<User>>>
    getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<User> userStream = response.items().stream().map(userDynamoDbMapper::toUser);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), userStream);
                });
    }


    @Override
    public CompletableFuture<User> findById(String id) {
        return dynamoDB.getItem(userDynamoDbRepository.getByKeySchemaRequest(id)).thenApply(response -> userDynamoDbMapper.toUser(response.item()));
    }

    @Override
    public CompletableFuture<List<User>> findAll() {
        return dynamoDB.scan(userDynamoDbRepository.scanRequest(null))
                .thenApply(res -> {
                    Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                    Stream<User> organizationStream = res.items().stream().map(userDynamoDbMapper::toUser);
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<User>> nextPage
                                = this.getPage(userDynamoDbRepository.scanRequest(lastKeyEvaluated)).join();
                        lastKeyEvaluated = nextPage.getKey();
                        organizationStream = Stream.concat(organizationStream, nextPage.getValue());
                    }
                    return organizationStream.collect(Collectors.toList());
                });
    }

    @Override
    public CompletableFuture<User> add(User user) {
        user.setId(UUID.randomUUID().toString());
        return dynamoDB.putItem(userDynamoDbRepository.putRequest(userDynamoDbMapper.toDynamoDbItem(user))).thenApply(response -> userDynamoDbMapper.toUser(response.attributes()));
    }
}
