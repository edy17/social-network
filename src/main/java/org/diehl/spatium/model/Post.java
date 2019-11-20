package org.diehl.spatium.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RegisterForReflection
public class Post extends AbstractBaseEntity {

    private static Logger logger = Logger.getLogger("org.diehl.spatium.model.Post");

    private String description;
    private byte[] image;
    private Date instant;
    private boolean isVisible;
    private int reportsNumber;
    private String userId;

    public static Post from(Map<String, AttributeValue> item) {
        Post post = new Post();
        if (item != null && !item.isEmpty()) {
            Arrays.stream(Post.class.getDeclaredFields()).forEach(field -> {
                try {
                    field.setAccessible(true);
                    field.set(post, item.get(field.getName()).s());


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

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Date getInstant() {
        return instant;
    }

    public void setInstant(Date instant) {
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
}
