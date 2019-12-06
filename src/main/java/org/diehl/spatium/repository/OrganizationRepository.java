package org.diehl.spatium.repository;

import org.diehl.spatium.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import javax.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class OrganizationRepository extends AbstractDynamoDbRepository<Organization> {

    private static final Logger logger = LoggerFactory.getLogger("org.diehl.spatium.repository.OrganizationRepository");

    private static final String TABLE_NAME = "Organization";
    private static final String KEY_SCHEMA = "name";
    private static final String POSTS_COLUMN = "postIds";
    private static final String USER_IDS_OF_MEMBERS = "userIdsOfMembers";

    private static final List<String> columns = Stream.of(Organization.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());

    public UpdateItemRequest addPostRequest(Organization organization) {
        Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
        updatedValues.put(POSTS_COLUMN, AttributeValueUpdate
                .builder().value(AttributeValue.builder().ss(organization.getPostIds()).build()).action(AttributeAction.PUT).build());
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(KEY_SCHEMA, AttributeValue.builder().s(organization.getName()).build());
        return UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(item)
                .attributeUpdates(updatedValues)
                .build();
    }

    @Override
    public PutItemRequest putRequest(Organization organization) {
        Map<String, AttributeValue> item = new HashMap<>();
        Arrays.asList(Organization.class.getDeclaredFields()).forEach(field -> {
            try {
                field.setAccessible(true);
                if (field.get(organization) != null) {
                    if (field.getName().equals(POSTS_COLUMN)) {
                        item.put(field.getName(), AttributeValue.builder().ss(organization.getPostIds()).build());
                    } else if (field.getName().equals(USER_IDS_OF_MEMBERS)) {
                        item.put(field.getName(), AttributeValue.builder().ss(organization.getUserIdsOfMembers()).build());
                    } else {
                        item.put(field.getName(), AttributeValue.builder().s((String) field.get(organization)).build());
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error(String.format("An exception occurred when accede field %s of Organisation object by reflection!", field.getName()), e);
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
            Arrays.asList(Organization.class.getDeclaredFields()).forEach(field -> {
                try {
                    if (item.containsKey(field.getName())) {
                        field.setAccessible(true);
                        if (field.getName().equals(POSTS_COLUMN)) {
                            organization.setPostIds(item.get(field.getName()).ss());
                        } else if (field.getName().equals(USER_IDS_OF_MEMBERS)) {
                            organization.setUserIdsOfMembers(item.get(field.getName()).ss());
                        } else {
                            field.set(organization, item.get(field.getName()).s());
                        }
                    }
                } catch (IllegalAccessException e) {
                    logger.error(String.format("An exception occurred when set field %s of Organisation object by reflection!", field.getName()), e);
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
