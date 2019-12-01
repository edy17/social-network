package org.diehl.spatium.repository;

import org.diehl.spatium.model.AbstractBaseEntity;
import org.diehl.spatium.model.User;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import javax.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class UserRepository extends AbstractBaseRepository<User> {

    private static Logger logger = Logger.getLogger("org.diehl.spatium.repository.UserRepository");
    private static final String TABLE_NAME = "User";
    private static final List<String> columns = Stream.of(AbstractBaseEntity.class.getDeclaredFields(), User.class.getDeclaredFields()).flatMap(Stream::of).map(Field::getName).collect(Collectors.toList());

    @Override
    public PutItemRequest putRequest(User user) {
        Map<String, AttributeValue> item = new HashMap<>();
        Stream.of(AbstractBaseEntity.class.getDeclaredFields(), User.class.getDeclaredFields()).flatMap(Stream::of).forEach(field -> {
            try {
                if (field.get(user) != null) {
                    item.put(field.getName(), AttributeValue.builder().s((String) field.get(user)).build());
                }
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "An exception was thrown: ", e);
            }
        });
        return PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
    }

    @Override
    public User getObject(Map<String, AttributeValue> item) {
        User user = new User();
        if (item != null && !item.isEmpty()) {
            Stream.of(AbstractBaseEntity.class.getDeclaredFields(), User.class.getDeclaredFields()).flatMap(Stream::of).forEach(field -> {
                try {
                    if (item.containsKey(field.getName())) {
                        field.setAccessible(true);
                        field.set(user, item.get(field.getName()).s());
                    }
                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE, "An exception was thrown: ", e);
                }
            });
        }
        return user;
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public List<String> getColumns() {
        return columns;
    }
}
