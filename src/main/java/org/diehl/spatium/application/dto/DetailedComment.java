package org.diehl.spatium.application.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.diehl.spatium.domain.model.Comment;
import org.diehl.spatium.domain.model.User;

import java.io.Serializable;

@RegisterForReflection
public class DetailedComment implements Serializable {

    private Comment comment;
    private User user;

    public DetailedComment(Comment comment, User user) {
        this.comment = comment;
        this.user = user;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
