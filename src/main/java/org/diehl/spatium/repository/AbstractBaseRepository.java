package org.diehl.spatium.repository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBaseRepository<T> {

    public ScanRequest scanRequest() {
        return ScanRequest.builder().tableName(this.getTableName()).attributesToGet(this.getColumns()).build();
    }

    public GetItemRequest getByIdRequest(String id) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(id).build());
        return GetItemRequest.builder()
                .tableName(this.getTableName())
                .key(item)
                .attributesToGet(this.getColumns())
                .build();
    }

    public abstract PutItemRequest putRequest(T abstractBaseEntity);

    public abstract String getTableName();

    public abstract List<String> getColumns();
}
