package org.diehl.spatium.application;

import org.diehl.spatium.application.dto.DetailedComment;
import org.diehl.spatium.application.dto.DetailedPost;
import org.diehl.spatium.domain.model.Comment;
import org.diehl.spatium.domain.model.Organization;
import org.diehl.spatium.domain.model.Post;
import org.diehl.spatium.domain.model.User;
import org.diehl.spatium.domain.service.CommentService;
import org.diehl.spatium.domain.service.OrganizationService;
import org.diehl.spatium.domain.service.PostService;
import org.diehl.spatium.domain.service.UserService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class SpatiumAPI {

    @Inject
    PostService postService;
    @Inject
    UserService userService;
    @Inject
    CommentService commentService;
    @Inject
    OrganizationService organizationService;

    public CompletableFuture<List<DetailedPost>> findPublicDetailedPosts() {
        return postService.findByPublic()
                .thenApply(this::getDetailedPostsFromPosts);
    }

    public CompletableFuture<List<DetailedPost>> findOrganizationDetailedPosts(String organizationId) {
        return postService.findByOrganization(organizationId)
                .thenApply(this::getDetailedPostsFromPosts);
    }

    private List<DetailedPost> getDetailedPostsFromPosts(List<Post> posts) {
        List<DetailedPost> detailedPosts = new ArrayList<>();
        posts.forEach(post -> {
            DetailedPost dp = new DetailedPost();
            dp.setPost(post);
            User user = userService.findById(post.getUserId()).join();
            dp.setUser(user);
            List<Comment> comments = commentService.findByPostId(post.getId()).join();
            List<DetailedComment> detailedComments = new ArrayList<>();
            comments.forEach(comment -> {
                User userComment = userService.findById(comment.getUserId()).join();
                detailedComments.add(new DetailedComment(comment, userComment));
            });
            dp.setComments(detailedComments);
            detailedPosts.add(dp);
        });
        return detailedPosts;
    }

    public CompletableFuture<Post> findPostById(String id) {
        return postService.findById(id);
    }

    public CompletableFuture<Post> addPost(Post post) {
        return postService.add(post);
    }

    public InputStream getImage(String key) {
        return postService.getImage(key);
    }

    public CompletableFuture<List<Comment>> findCommentsByPostId(String id) {
        return commentService.findByPostId(id);
    }

    public CompletableFuture<Comment> addComment(Comment comment) {
        return commentService.add(comment);
    }

    public CompletableFuture<List<Organization>> findAllOrganizations() {
        return organizationService.findAll();
    }

    public CompletableFuture<Organization> findOrganizationById(String id) {
        return organizationService.findById(id);
    }

    public CompletableFuture<Organization> addOrganization(Organization organization) {
        return organizationService.add(organization);
    }

    public CompletableFuture<List<User>> findAllUsers() {
        return userService.findAll();
    }

    public CompletableFuture<User> findUserById(String id) {
        return userService.findById(id);
    }

    public CompletableFuture<User> addUser(User user) {
        return userService.add(user);
    }
}
