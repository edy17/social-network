package org.diehl.spatium.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Objects;

@RegisterForReflection
public class Comment implements Serializable {

    private String id;
    private String content;
    private DateTime instant;
    private String userId;
    private String postId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DateTime getInstant() {
        return instant;
    }

    public void setInstant(DateTime instant) {
        this.instant = instant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment that = (Comment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
