package org.diehl.spatium.repository;


import org.diehl.spatium.model.AbstractBaseEntity;
import org.diehl.spatium.model.Post;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import software.amazon.awssdk.core.SdkBytes;
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
public class PostRepository extends AbstractBaseRepository<Post> {

    private static Logger logger = Logger.getLogger("org.diehl.spatium.repository.PostRepository");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final String tableName = "Post";
    private static final List<String> columns = Stream.of(AbstractBaseEntity.class.getDeclaredFields(), Post.class.getDeclaredFields()).flatMap(Stream::of).map(Field::getName).collect(Collectors.toList());

    @Override
    public PutItemRequest putRequest(Post post) {
        post.setInstant(dateTimeFormatter.print(new DateTime(DateTimeZone.UTC)));
        Map<String, AttributeValue> item = new HashMap<>();
        Stream.of(AbstractBaseEntity.class.getDeclaredFields(), Post.class.getDeclaredFields()).flatMap(Stream::of).forEach(field -> {
            try {
                if (field.get(post) != null) {
                    if (field.getName().equals("image")) {
                        item.put(field.getName(), AttributeValue.builder().b(SdkBytes.fromByteArray((byte[]) field.get(post))).build());
                    } else if (field.getName().equals("reportsNumber")) {
                        field.set(post, Integer.parseInt(item.get(field.getName()).n()));
                        item.put(field.getName(), AttributeValue.builder().n(String.valueOf((int) field.get(post))).build());
                    } else if (field.getName().equals("isVisible")) {
                        item.put(field.getName(), AttributeValue.builder().bool((boolean) field.get(post)).build());
                    } else {
                        item.put(field.getName(), AttributeValue.builder().s((String) field.get(post)).build());
                    }
                }
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "An exception was thrown: ", e);
            }
        });
        return PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public List<String> getColumns() {
        return columns;
    }
}
