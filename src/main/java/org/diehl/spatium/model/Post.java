package org.diehl.spatium.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@RegisterForReflection
public class Post extends AbstractBaseEntity {

    private static Logger logger = Logger.getLogger("org.diehl.spatium.model.Post");
    private String description;
    @JsonIgnore
    private byte[] image;
    private String instant;
    private boolean isVisible;
    private int reportsNumber;
    private String userId;
    private List<Comment> comments;

    public static Post from(Map<String, AttributeValue> item) {
        Post post = new Post();
        if (item != null && !item.isEmpty()) {
            Stream.of(AbstractBaseEntity.class.getDeclaredFields(), Post.class.getDeclaredFields()).flatMap(Stream::of).forEach(field -> {
                try {
                    if (item.containsKey(field.getName())) {
                        field.setAccessible(true);
                        if (field.getName().equals("image")) {
                            field.set(post, item.get(field.getName()).b().asByteArray());
                        } else if (field.getName().equals("reportsNumber")) {
                            field.set(post, Integer.parseInt(item.get(field.getName()).n()));
                        } else if (field.getName().equals("isVisible")) {
                            field.set(post, item.get(field.getName()).bool());
                        } else if (!(field.getName().equals("logger") || field.getName().equals("comments"))) {
                            field.set(post, item.get(field.getName()).s());
                        }
                    }
                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE, "An exception was thrown: ", e);
                }
            });
        }
        return post;
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

    public String getInstant() {
        return instant;
    }

    public void setInstant(String instant) {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
