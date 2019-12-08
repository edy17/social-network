package org.diehl.spatium.infrastructure.dynamodb.service;

import org.diehl.spatium.domain.model.User;
import org.diehl.spatium.domain.service.UserService;
import org.diehl.spatium.infrastructure.dynamodb.mapper.UserDynamoMapper;
import org.diehl.spatium.infrastructure.dynamodb.repository.UserRepository;
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
public class UserDynamoService implements UserService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    UserRepository userRepository;
    @Inject
    UserDynamoMapper userDynamoMapper;

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<User>>>
    getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<User> userStream = response.items().stream().map(userDynamoMapper::toUser);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), userStream);
                });
    }


    @Override
    public CompletableFuture<User> findById(String id) {
        return dynamoDB.getItem(userRepository.getByKeySchemaRequest(id)).thenApply(response -> userDynamoMapper.toUser(response.item()));
    }

    @Override
    public CompletableFuture<List<User>> findAll() {
        return dynamoDB.scan(userRepository.scanRequest(null))
                .thenApply(res -> {
                    Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                    Stream<User> organizationStream = res.items().stream().map(userDynamoMapper::toUser);
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<User>> nextPage
                                = this.getPage(userRepository.scanRequest(lastKeyEvaluated)).join();
                        lastKeyEvaluated = nextPage.getKey();
                        organizationStream = Stream.concat(organizationStream, nextPage.getValue());
                    }
                    return organizationStream.collect(Collectors.toList());
                });
    }

    @Override
    public CompletableFuture<User> add(User user) {
        user.setId(UUID.randomUUID().toString());
        return dynamoDB.putItem(userRepository.putRequest(userDynamoMapper.toDynamoDbItem(user))).thenApply(response -> userDynamoMapper.toUser(response.attributes()));
    }
}
