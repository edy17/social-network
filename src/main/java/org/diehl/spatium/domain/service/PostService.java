package org.diehl.spatium.domain.service;

import org.diehl.spatium.domain.model.Post;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PostService {

    CompletableFuture<Post> findById(String id);

    CompletableFuture<List<Post>> findByPublic();

    CompletableFuture<List<Post>> findByOrganization(String organizationId);

    CompletableFuture<Post> add(Post post);

    InputStream getImage(String key);
}
