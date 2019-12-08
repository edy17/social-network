package org.diehl.spatium.infrastructure.dynamodb.repository;


import org.diehl.spatium.domain.model.Post;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ApplicationScoped
public class PostRepository extends AbstractDynamoDbRepository<Post> {

    private static final String TABLE_NAME = "Post";
    private static final String KEY_SCHEMA = "id";
    private static final List<String> columns = Stream.of(Post.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());


    public ScanRequest scanByPublicRequest(Map<String, AttributeValue> lastKey) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":val", AttributeValue.builder().bool(true).build());
        return ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("isPublic = :val")
                .expressionAttributeValues(expressionAttributeValues)
                .exclusiveStartKey(lastKey)
                .build();
    }

    public ScanRequest scanByOrganization(String organizationId, Map<String, AttributeValue> lastKey) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":val", AttributeValue.builder().s(organizationId).build());
        return ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("organizationId = :val")
                .expressionAttributeValues(expressionAttributeValues)
                .exclusiveStartKey(lastKey)
                .build();
    }

    @Override
    public PutItemRequest putRequest(Map<String, AttributeValue> item) {
        return PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
    }


    @Override
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
