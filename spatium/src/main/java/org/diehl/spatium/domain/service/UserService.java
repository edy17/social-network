package org.diehl.spatium.domain.service;

import org.diehl.spatium.domain.model.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {

    CompletableFuture<User> findById(String id);

    CompletableFuture<List<User>> findAll();

    CompletableFuture<User> add(User user);
}
