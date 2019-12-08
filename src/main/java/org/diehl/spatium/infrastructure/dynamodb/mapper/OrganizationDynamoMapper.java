package org.diehl.spatium.infrastructure.dynamodb.mapper;

import org.diehl.spatium.domain.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class OrganizationDynamoMapper {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationDynamoMapper.class);
    private static final String USER_IDS_OF_MEMBERS_COLUMN = "userIdsOfMembers";

    public Map<String, AttributeValue> toDynamoDbItem(Organization organization) {
        Map<String, AttributeValue> item = new HashMap<>();
        Arrays.asList(Organization.class.getDeclaredFields()).forEach(field -> {
            try {
                field.setAccessible(true);
                if (field.get(organization) != null) {
                    if (field.getName().equals(USER_IDS_OF_MEMBERS_COLUMN)) {
                        item.put(field.getName(), AttributeValue.builder().ss(organization.getUserIdsOfMembers()).build());
                    } else {
                        item.put(field.getName(), AttributeValue.builder().s((String) field.get(organization)).build());
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error(String.format("An exception occurred when accessing field %s of Organisation object by reflection!", field.getName()), e);
            }
        });
        return item;
    }

    public Organization toOrganization(Map<String, AttributeValue> item) {
        Organization organization = new Organization();
        if (item != null && !item.isEmpty()) {
            Arrays.asList(Organization.class.getDeclaredFields()).forEach(field -> {
                try {
                    if (item.containsKey(field.getName())) {
                        field.setAccessible(true);
                        if (field.getName().equals(USER_IDS_OF_MEMBERS_COLUMN)) {
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
}
