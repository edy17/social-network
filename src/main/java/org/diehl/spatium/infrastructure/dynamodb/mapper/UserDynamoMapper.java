package org.diehl.spatium.infrastructure.dynamodb.mapper;

import org.diehl.spatium.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class UserDynamoMapper {

    private static Logger logger = LoggerFactory.getLogger(UserDynamoMapper.class);

    public Map<String, AttributeValue> toDynamoDbItem(User user) {
        Map<String, AttributeValue> item = new HashMap<>();
        Arrays.asList(User.class.getDeclaredFields()).forEach(field -> {
            try {
                field.setAccessible(true);
                if (field.get(user) != null) {
                    item.put(field.getName(), AttributeValue.builder().s((String) field.get(user)).build());
                }
            } catch (IllegalAccessException e) {
                logger.error("An exception occurred when accede field " + field.getName()
                        + " of User object by reflection!", e);
            }
        });
        return item;
    }

    public User toUser(Map<String, AttributeValue> item) {
        User user = new User();
        if (item != null && !item.isEmpty()) {
            Arrays.asList(User.class.getDeclaredFields()).forEach(field -> {
                try {
                    if (item.containsKey(field.getName())) {
                        field.setAccessible(true);
                        field.set(user, item.get(field.getName()).s());
                    }
                } catch (IllegalAccessException e) {
                    logger.error("An exception occurred when accessing field " + field.getName()
                            + " of User object by reflection!", e);
                }
            });
        }
        return user;
    }
}
