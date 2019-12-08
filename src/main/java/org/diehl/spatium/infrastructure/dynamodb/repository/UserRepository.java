package org.diehl.spatium.infrastructure.dynamodb.repository;

import org.diehl.spatium.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import javax.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class UserRepository extends AbstractDynamoDbRepository<User> {

    private static final String TABLE_NAME = "User";
    private static final String KEY_SCHEMA = "id";
    private static final List<String> columns = Stream.of(User.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());

    @Override
    public PutItemRequest putRequest(Map<String, AttributeValue> item) {
        return PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
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
