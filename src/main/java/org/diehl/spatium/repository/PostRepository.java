package org.diehl.spatium.repository;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.diehl.spatium.model.AbstractBaseEntity;
import org.diehl.spatium.model.Comment;
import org.diehl.spatium.model.Post;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import javax.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ApplicationScoped
public class PostRepository extends AbstractBaseRepository<Post> {

    private static Logger logger = LoggerFactory.getLogger("org.diehl.spatium.repository.PostRepository");
    private static final String TABLE_NAME = "Post";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final List<String> columns = Stream.of(AbstractBaseEntity.class.getDeclaredFields(), Post.class.getDeclaredFields()).flatMap(Stream::of).map(Field::getName).collect(Collectors.toList());
    private ObjectMapper mapper;

    public PostRepository() {
        mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
    }

    @Override
    public PutItemRequest putRequest(Post post) {
        Map<String, AttributeValue> item = new HashMap<>();
        Stream.of(AbstractBaseEntity.class.getDeclaredFields(), Post.class.getDeclaredFields()).flatMap(Stream::of).forEach(field -> {
            try {
                if (field.get(post) != null) {
                    if (field.getName().equals("image")) {
                        item.put(field.getName(), AttributeValue.builder().b(SdkBytes.fromByteArray(post.getImage())).build());
                    } else if (field.getName().equals("instant")) {
                        item.put(field.getName(), AttributeValue.builder().s(dateTimeFormatter.print(post.getInstant())).build());
                    } else if (field.getName().equals("reportsNumber")) {
                        item.put(field.getName(), AttributeValue.builder().n(String.valueOf(post.getReportsNumber())).build());
                    } else if (field.getName().equals("isVisible")) {
                        item.put(field.getName(), AttributeValue.builder().bool(post.isVisible()).build());
                    } else if (field.getName().equals("comments")) {
                        String json = mapper.writeValueAsString(post.getComments());
                        item.put(field.getName(), AttributeValue.builder().s(json).build());
                    } else {
                        item.put(field.getName(), AttributeValue.builder().s((String) field.get(post)).build());
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
    public Post getObject(Map<String, AttributeValue> item) {
        Post post = new Post();
        if (item != null && !item.isEmpty()) {
            Stream.of(AbstractBaseEntity.class.getDeclaredFields(), Post.class.getDeclaredFields()).flatMap(Stream::of).forEach(field -> {
                try {
                    if (item.containsKey(field.getName())) {
                        field.setAccessible(true);
                        if (field.getName().equals("image")) {
                            post.setImage(item.get(field.getName()).b().asByteArray());
                        } else if (field.getName().equals("instant")) {
                            post.setInstant(dateTimeFormatter.parseDateTime(item.get(field.getName()).s()));
                        } else if (field.getName().equals("reportsNumber")) {
                            post.setReportsNumber(Integer.parseInt(item.get(field.getName()).n()));
                        } else if (field.getName().equals("isVisible")) {
                            post.setVisible(item.get(field.getName()).bool());
                        } else if (field.getName().equals("comments")) {
                            String json = item.get(field.getName()).s();
                            List<Comment> comments = mapper.readValue(json, new TypeReference<List<Comment>>() {
                            });
                            post.setComments(comments);
                        } else {
                            field.set(post, item.get(field.getName()).s());
                        }
                    }
                } catch (Exception e) {
                    logger.error("An exception occurred!", e);
                }
            });
        }
        return post;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public List<String> getColumns() {
        return columns;
    }
}
