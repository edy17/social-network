package org.diehl.spatium.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.diehl.spatium.model.AbstractBaseEntity;
import org.diehl.spatium.model.Organization;
import org.diehl.spatium.model.Post;
import org.diehl.spatium.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import javax.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class OrganizationRepository extends AbstractBaseRepository<Organization> {

    private static final Logger logger = LoggerFactory.getLogger("org.diehl.spatium.repository.OrganizationRepository");

    private static final String TABLE_NAME = "Organization";
    private static final String KEY_SCHEMA = "name";
    private static final String POSTS_COLUMN = "posts";
    private static final String USERS_COLUMN = "users";
    private static final List<String> columns = Stream.of(AbstractBaseEntity.class.getDeclaredFields(), Organization.class.getDeclaredFields()).flatMap(Stream::of).map(Field::getName).collect(Collectors.toList());
    private ObjectMapper mapper;

    public OrganizationRepository() {
        mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
    }

    public UpdateItemRequest update(Organization organization) {

        Map<String, AttributeValueUpdate> updated_values = new HashMap<>();
        Stream.of(Organization.class.getDeclaredFields()).forEach(field -> {
            try {
                if (field.get(organization) != null) {
                    if (field.getName().equals(POSTS_COLUMN)) {
                        String json = mapper.writeValueAsString(organization.getPosts());
                        updated_values.put(field.getName(), AttributeValueUpdate
                                .builder().value(AttributeValue.builder().s(json).build()).action(AttributeAction.PUT).build());
                    } else if (field.getName().equals(USERS_COLUMN)) {
                        String json = mapper.writeValueAsString(organization.getUsers());
                        updated_values.put(field.getName(), AttributeValueUpdate
                                .builder().value(AttributeValue.builder().s(json).build()).action(AttributeAction.PUT).build());
                    } else {
                        updated_values.put(field.getName(), AttributeValueUpdate
                                .builder().value(AttributeValue.builder().s((String) field.get(organization)).build()).action(AttributeAction.PUT).build());
                    }
                }
            } catch (Exception e) {
                logger.warn("An exception was thrown", e);
            }
        });
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("name", AttributeValue.builder().s(organization.getName()).build());
        return UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(item)
                .attributeUpdates(updated_values)
                .build();
    }

    @Override
    public PutItemRequest putRequest(Organization organization) {
        Map<String, AttributeValue> item = new HashMap<>();
        Stream.of(Organization.class.getDeclaredFields()).forEach(field -> {
            try {
                field.setAccessible(true);
                if (field.get(organization) != null) {
                    if (field.getName().equals(POSTS_COLUMN)) {
                        String json = mapper.writeValueAsString(organization.getPosts());
                        item.put(field.getName(), AttributeValue.builder().s(json).build());
                    } else if (field.getName().equals(USERS_COLUMN)) {
                        String json = mapper.writeValueAsString(organization.getUsers());
                        item.put(field.getName(), AttributeValue.builder().s(json).build());
                    } else {
                        item.put(field.getName(), AttributeValue.builder().s((String) field.get(organization)).build());
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred!", e);
            }
        });
        return PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
    }

    @Override
    public Organization getObject(Map<String, AttributeValue> item) {
        Organization organization = new Organization();
        if (item != null && !item.isEmpty()) {
            Stream.of(Organization.class.getDeclaredFields()).forEach(field -> {
                try {
                    if (item.containsKey(field.getName())) {
                        field.setAccessible(true);
                        if (field.getName().equals(POSTS_COLUMN)) {
                            String json = item.get(field.getName()).s();
                            List<Post> posts = mapper.readValue(json, new TypeReference<List<Post>>() {
                            });
                            organization.setPosts(posts.stream().map(Post::getId).collect(Collectors.toList()));
                        } else if (field.getName().equals(USERS_COLUMN)) {
                            String json = item.get(field.getName()).s();
                            List<User> users = mapper.readValue(json, new TypeReference<List<User>>() {
                            });
                            organization.setUsers(users.stream().map(User::getId).collect(Collectors.toList()));
                        } else {
                            field.set(organization, item.get(field.getName()).s());
                        }
                    }
                } catch (Exception e) {
                    logger.error("An exception occurred!", e);
                }
            });
        }
        return organization;
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
