package org.diehl.spatium.repository;


import org.diehl.spatium.model.Post;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ApplicationScoped
public class PostRepository {

    private static Logger logger = Logger.getLogger("org.diehl.spatium.repository.PostRepository");
    private static final String TABLE_NAME = "Post";
    private static final Stream<Field> fieldStream = Arrays.stream(Post.class.getDeclaredFields());
    private static final List<String> columns = fieldStream.map(Field::getName).collect(Collectors.toList());

    public ScanRequest scanRequest() {
        return ScanRequest.builder().tableName(TABLE_NAME).attributesToGet(columns).build();
    }

    public PutItemRequest putRequest(Post post) {
        Map<String, AttributeValue> item = new HashMap<>();
        fieldStream.forEach(field -> {
            try {
                if (field.getName().equals("image")) {
                    item.put(field.getName(), AttributeValue.builder().b(SdkBytes.fromByteArray((byte[])field.get(post))).build());
                } else if (field.getName().equals("instant")) {
                    item.put(field.getName(), AttributeValue.builder().bool((boolean) field.get(post)).build());
                } else {
                    item.put(field.getName(), AttributeValue.builder().s((String) field.get(post)).build());
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

    public GetItemRequest getByIdRequest(String id) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(id).build());
        return GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(item)
                .attributesToGet(columns)
                .build();
    }
}
