package org.diehl.spatium.infrastructure.aws.dynamodb;

import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AbstractDynamoDbRepository<T> {

    PutItemRequest putRequest(Map<String, AttributeValue> item);

    String getTableName();

    List<String> getColumns();

    String getKeySchema();

    default ScanRequest scanRequest(Map<String, AttributeValue> lastKey) {
        return ScanRequest.builder()
                .tableName(this.getTableName())
                .attributesToGet(this.getColumns())
                .exclusiveStartKey(lastKey)
                .build();
    }

    default GetItemRequest getByKeySchemaRequest(String id) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(getKeySchema(), AttributeValue.builder().s(id).build());
        return GetItemRequest.builder()
                .tableName(this.getTableName())
                .key(item)
                .attributesToGet(this.getColumns())
                .build();
    }

    default CreateTableRequest createTableRequest() {
        return CreateTableRequest.builder()
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName(getKeySchema())
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .keySchema(KeySchemaElement.builder()
                        .attributeName(getKeySchema())
                        .keyType(KeyType.HASH)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build())
                .tableName(getTableName())
                .build();
    }
}
