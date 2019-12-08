package org.diehl.spatium.infrastructure.dynamodb.mapper;

import org.diehl.spatium.domain.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.enterprise.context.ApplicationScoped;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CommentDynamoMapper {

    private static Logger logger = LoggerFactory.getLogger(CommentDynamoMapper.class);
    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

    public Map<String, AttributeValue> toDynamoDbItem(Comment comment) {
        Map<String, AttributeValue> item = new HashMap<>();
        Arrays.asList(Comment.class.getDeclaredFields()).forEach(field -> {
            try {
                field.setAccessible(true);
                if (field.get(comment) != null) {
                    if (field.getName().equals("instant")) {
                        item.put(field.getName(), AttributeValue.builder().s(dateTimeFormatter.format(comment.getInstant())).build());
                    } else {
                        item.put(field.getName(), AttributeValue.builder().s((String) field.get(comment)).build());
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error("An exception occurred when accessing field " + field.getName()
                        + " of Comment object by reflection!", e);
            }
        });
        return item;
    }

    public Comment toComment(Map<String, AttributeValue> item) {
        Comment comment = new Comment();
        if (item != null && !item.isEmpty()) {
            Arrays.asList(Comment.class.getDeclaredFields()).forEach(field -> {
                try {
                    if (item.containsKey(field.getName())) {
                        field.setAccessible(true);
                        if (field.getName().equals("instant")) {
                            comment.setInstant(dateTimeFormatter.parse(item.get(field.getName()).s()));
                        } else {
                            field.set(comment, item.get(field.getName()).s());
                        }
                    }
                } catch (IllegalAccessException e) {
                    logger.error("An exception occurred when accessing field " + field.getName()
                            + " of User object by reflection!", e);
                } catch (ParseException e) {
                    logger.error("An exception occurred on parsing string datetime when getting item to comment!", e);
                }
            });
        }
        return comment;
    }
}
