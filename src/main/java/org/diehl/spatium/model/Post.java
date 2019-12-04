package org.diehl.spatium.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@RegisterForReflection
public class Post implements Serializable {

    private String id;
    private String description;
    @JsonIgnore
    @NotNull(message = "Please set an image")
    private byte[] image;
    private DateTime instant;
    private boolean isVisible;
    private int reportsNumber;
    @NotNull(message = "Please set organization")
    private String organizationId;
    @NotNull(message = "Please set user")
    private String userId;
    private List<Comment> comments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getImage() {
        return image;
    }

    @FormParam("image")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public void setImage(byte[] image) {
        this.image = image;
    }

    public DateTime getInstant() {
        return instant;
    }

    public void setInstant(DateTime instant) {
        this.instant = instant;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public int getReportsNumber() {
        return reportsNumber;
    }

    public void setReportsNumber(int reportsNumber) {
        this.reportsNumber = reportsNumber;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post that = (Post) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
