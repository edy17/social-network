package org.diehl.spatium.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RegisterForReflection
public class Comment extends AbstractBaseEntity {

    private static Logger logger = Logger.getLogger("org.diehl.spatium.model.Comment");


    private String content;
    private Date date;
    private String userId;
    private String postId;

    public static Comment from(Map<String, AttributeValue> item) {
        Comment comment = new Comment();
        if (item != null && !item.isEmpty()) {
            Arrays.stream(Comment.class.getDeclaredFields()).forEach(field -> {
                try {
                    field.setAccessible(true);
                    field.set(comment, item.get(field.getName()).s());


                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE, "An exception was thrown: ", e);
                }
            });
        }
        return comment;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
