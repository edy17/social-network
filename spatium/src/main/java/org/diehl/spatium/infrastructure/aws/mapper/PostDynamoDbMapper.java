package org.diehl.spatium.infrastructure.aws.mapper;

import org.diehl.spatium.domain.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.enterprise.context.ApplicationScoped;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class PostDynamoDbMapper {

    private static Logger logger = LoggerFactory.getLogger(PostDynamoDbMapper.class);
    private static final String IMAGE_COLUMN = "image";
    private static final String INSTANT_COLUMN = "instant";
    private static final String REPORTS_COLUMN = "reportsNumber";
    private static final String IS_PUBLIC_COLUMN = "isPublic";

    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

    public Map<String, AttributeValue> toDynamoDbItem(Post post) {
        Map<String, AttributeValue> item = new HashMap<>();
        Arrays.asList(Post.class.getDeclaredFields()).forEach(field -> {
            try {
                field.setAccessible(true);
                if ((field.get(post) != null) && (!field.getName().equals(IMAGE_COLUMN))) {
                    if (field.getName().equals(INSTANT_COLUMN)) {
                        item.put(field.getName(), AttributeValue.builder().s(dateTimeFormatter.format(post.getInstant())).build());
                    } else if (field.getName().equals(REPORTS_COLUMN)) {
                        item.put(field.getName(), AttributeValue.builder().n(String.valueOf(post.getReportsNumber())).build());
                    } else if (field.getName().equals(IS_PUBLIC_COLUMN)) {
                        item.put(field.getName(), AttributeValue.builder().bool(post.isPublic()).build());
                    } else {
                        item.put(field.getName(), AttributeValue.builder().s((String) field.get(post)).build());
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error(String.format("An exception occurred when accessing field %s of Post object by reflection!", field.getName()), e);
            }
        });
        return item;
    }

    public Post toPost(Map<String, AttributeValue> item) {
        Post post = new Post();
        if (item != null && !item.isEmpty()) {
            Arrays.asList(Post.class.getDeclaredFields()).forEach(field -> {
                try {
                    if ((item.containsKey(field.getName())) && (!field.getName().equals(IMAGE_COLUMN))) {
                        field.setAccessible(true);
                        if (field.getName().equals(INSTANT_COLUMN)) {
                            post.setInstant(dateTimeFormatter.parse(item.get(field.getName()).s()));
                        } else if (field.getName().equals(REPORTS_COLUMN)) {
                            post.setReportsNumber(Integer.parseInt(item.get(field.getName()).n()));
                        } else if (field.getName().equals(IS_PUBLIC_COLUMN)) {
                            post.setPublic(item.get(field.getName()).bool());
                        } else {
                            field.set(post, item.get(field.getName()).s());
                        }
                    }
                } catch (IllegalAccessException e) {
                    logger.error(String.format("An exception occurred when set field %s of Post object by reflection!", field.getName()), e);
                } catch (ParseException e) {
                    logger.error("An exception occurred on parsing string datetime when getting item to post!", e);
                }
            });
        }
        return post;
    }
}
