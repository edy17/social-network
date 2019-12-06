package org.diehl.spatium.repository;

import org.diehl.spatium.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class CommentRepository extends AbstractDynamoDbRepository<Comment> {

    private static Logger logger = LoggerFactory.getLogger("org.diehl.spatium.repository.CommentRepository");
    private static final String TABLE_NAME = "Comment";
    private static final String KEY_SCHEMA = "id";
    private static final List<String> columns = Stream.of(Comment.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
    private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");


    public ScanRequest scanByPostIdRequest(String postId, Map<String, AttributeValue> lastKey) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":val", AttributeValue.builder().s(postId).build());
        return ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("postId = :val")
                .expressionAttributeValues(expressionAttributeValues)
                .exclusiveStartKey(lastKey)
                .build();
    }

    @Override
    public PutItemRequest putRequest(Comment comment) {
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
                logger.error("An exception occurred when accede field " + field.getName()
                        + " of Comment object by reflection!", e);
            }
        });
        return PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
    }

    @Override
    public Comment getObject(Map<String, AttributeValue> item) {
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
                    logger.error("An exception occurred when accede field " + field.getName()
                            + " of User object by reflection!", e);
                } catch (ParseException e) {
                    logger.error("An exception occurred when parse string datetime on getting a comment!", e);
                }
            });
        }
        return comment;
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public List<String> getColumns() {
        return columns;
    }

    @Override
    public String getKeySchema() {
        return KEY_SCHEMA;
    }
}
