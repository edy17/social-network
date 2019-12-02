package org.diehl.spatium.service;

import org.diehl.spatium.model.User;
import org.diehl.spatium.repository.UserRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    UserRepository userRepository;

    public CompletableFuture<List<User>> findAll() {
        return dynamoDB.scan(userRepository.scanRequest())
                .thenApply(res -> res.items().stream().map(userRepository::getObject).collect(Collectors.toList()));
    }

    public CompletableFuture<User> add(User user) {
        user.setId(UUID.randomUUID().toString());
        return dynamoDB.putItem(userRepository.putRequest(user)).thenApply(response -> userRepository.getObject(response.attributes()));
    }

    public CompletableFuture<User> getById(String id) {
        return dynamoDB.getItem(userRepository.getByIdRequest(id)).thenApply(response -> userRepository.getObject(response.item()));
    }
}
