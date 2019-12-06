package org.diehl.spatium.service;

import org.diehl.spatium.model.User;
import org.diehl.spatium.repository.UserRepository;
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
public class UserService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    UserRepository userRepository;

    public CompletableFuture<List<User>> findAll() {
        return dynamoDB.scan(userRepository.scanRequest(null))
                .thenApply(res -> {
                    Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                    Stream<User> organizationStream = res.items().stream().map(userRepository::getObject);
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<User>> nextPage
                                = this.getPage(userRepository.scanRequest(lastKeyEvaluated)).join();
                        lastKeyEvaluated = nextPage.getKey();
                        organizationStream = Stream.concat(organizationStream, nextPage.getValue());
                    }
                    return organizationStream.collect(Collectors.toList());
                });
    }

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<User>>> getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<User> userStream = response.items().stream().map(userRepository::getObject);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), userStream);
                });
    }

    public CompletableFuture<User> add(User user) {
        user.setId(UUID.randomUUID().toString());
        return dynamoDB.putItem(userRepository.putRequest(user)).thenApply(response -> userRepository.getObject(response.attributes()));
    }

    public CompletableFuture<User> getByKeySchema(String keySchema) {
        return dynamoDB.getItem(userRepository.getByKeySchemaRequest(keySchema)).thenApply(response -> userRepository.getObject(response.item()));
    }
}
