package org.diehl.spatium.domain.service;

import org.diehl.spatium.domain.model.Comment;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CommentService {

    CompletableFuture<List<Comment>> findByPostId(String postId);

    CompletableFuture<Comment> add(Comment comment);
}
