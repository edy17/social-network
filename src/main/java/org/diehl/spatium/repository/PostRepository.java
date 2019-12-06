package org.diehl.spatium.repository;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.diehl.spatium.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ApplicationScoped
public class PostRepository extends AbstractDynamoDbRepository<Post> {

    private static Logger logger = LoggerFactory.getLogger("org.diehl.spatium.repository.PostRepository");
    private static final String TABLE_NAME = "Post";
    private static final String KEY_SCHEMA = "id";
    private static final String IMAGE_COLUMN = "image";
    private static final String INSTANT_COLUMN = "instant";
    private static final String REPORTS_COLUMN = "reportsNumber";
    private static final String IS_PUBLIC_COLUMN = "isPublic";
    private static final String COMMENTS_COLUMN = "commentIds";
    private static final String ORGANIZATION_ID_COLUMN = "organizationId";


    private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
    private static final List<String> columns = Stream.of(Post.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
    private static final int IMG_WIDTH = 280;
    private static final int IMG_HEIGHT = 350;

    private void resizeImage(byte[] image, Post post) throws IOException {
        InputStream in = new ByteArrayInputStream(image);
        BufferedImage originalImage = ImageIO.read(in);
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "png", baos);
        baos.flush();
        post.setImage(baos.toByteArray());
        baos.close();
    }

    public ScanRequest scanByPublicRequest(Map<String, AttributeValue> lastKey) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":val", AttributeValue.builder().bool(true).build());
        return ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("isPublic = :val")
                .expressionAttributeValues(expressionAttributeValues)
                .exclusiveStartKey(lastKey)
                .build();
    }

    public ScanRequest scanByOrganization(String organizationId, Map<String, AttributeValue> lastKey) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":val", AttributeValue.builder().s(organizationId).build());
        return ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("organizationId = :val")
                .expressionAttributeValues(expressionAttributeValues)
                .exclusiveStartKey(lastKey)
                .build();
    }

    public UpdateItemRequest addCommentRequest(Post post) {
        Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
        updatedValues.put(COMMENTS_COLUMN, AttributeValueUpdate.builder().value(AttributeValue.builder().ss(post.getCommentIds()).build()).action(AttributeAction.PUT).build());
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(KEY_SCHEMA, AttributeValue.builder().s(post.getId()).build());
        return UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(item)
                .attributeUpdates(updatedValues)
                .build();
    }

    @Override
    public PutItemRequest putRequest(Post post) {
        Map<String, AttributeValue> item = new HashMap<>();
        Arrays.asList(Post.class.getDeclaredFields()).forEach(field -> {
            try {
                field.setAccessible(true);
                if (field.get(post) != null) {
                    if (field.getName().equals(IMAGE_COLUMN)) {
                        resizeImage(post.getImage(), post);
                        item.put(field.getName(), AttributeValue.builder().b(SdkBytes.fromByteArray(post.getImage())).build());
                    } else if (field.getName().equals(INSTANT_COLUMN)) {
                        item.put(field.getName(), AttributeValue.builder().s(dateTimeFormatter.format(post.getInstant())).build());
                    } else if (field.getName().equals(REPORTS_COLUMN)) {
                        item.put(field.getName(), AttributeValue.builder().n(String.valueOf(post.getReportsNumber())).build());
                    } else if (field.getName().equals(IS_PUBLIC_COLUMN)) {
                        item.put(field.getName(), AttributeValue.builder().bool(post.isPublic()).build());
                    } else if (field.getName().equals(COMMENTS_COLUMN)) {
                        item.put(field.getName(), AttributeValue.builder().ss(post.getCommentIds()).build());
                    } else {
                        item.put(field.getName(), AttributeValue.builder().s((String) field.get(post)).build());
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error(String.format("An exception occurred when accede field %s of Post object by reflection!", field.getName()), e);
            } catch (JsonProcessingException e) {
                logger.error("An exception occurred when convert objects list to json!", e);
            } catch (IOException e) {
                logger.error("An exception occurred when resize image!", e);
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
            Arrays.asList(Post.class.getDeclaredFields()).forEach(field -> {
                try {
                    if (item.containsKey(field.getName())) {
                        field.setAccessible(true);
                        if (field.getName().equals(IMAGE_COLUMN)) {
                            post.setImage(item.get(field.getName()).b().asByteArray());
                        } else if (field.getName().equals(INSTANT_COLUMN)) {
                            post.setInstant(dateTimeFormatter.parse(item.get(field.getName()).s()));
                        } else if (field.getName().equals(REPORTS_COLUMN)) {
                            post.setReportsNumber(Integer.parseInt(item.get(field.getName()).n()));
                        } else if (field.getName().equals(IS_PUBLIC_COLUMN)) {
                            post.setPublic(item.get(field.getName()).bool());
                        } else if (field.getName().equals(COMMENTS_COLUMN)) {
                            post.setCommentIds(item.get(field.getName()).ss());
                        } else {
                            field.set(post, item.get(field.getName()).s());
                        }
                    }
                } catch (IllegalAccessException e) {
                    logger.error(String.format("An exception occurred when set field %s of Post object by reflection!", field.getName()), e);
                } catch (ParseException e) {
                    logger.error("An exception occurred when parse string datetime on getting a post!", e);
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

    @Override
    public String getKeySchema() {
        return KEY_SCHEMA;
    }
}
