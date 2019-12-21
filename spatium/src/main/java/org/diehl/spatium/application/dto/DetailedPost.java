package org.diehl.spatium.application.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.diehl.spatium.domain.model.Post;
import org.diehl.spatium.domain.model.User;

import java.io.Serializable;
import java.util.List;

@RegisterForReflection
public class DetailedPost implements Serializable {

    private Post post;
    private User user;
    private List<DetailedComment> comments;

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<DetailedComment> getComments() {
        return comments;
    }

    public void setComments(List<DetailedComment> comments) {
        this.comments = comments;
    }
}
