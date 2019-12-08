package org.diehl.spatium.infrastructure.dynamodb.mapper;

import org.diehl.spatium.domain.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class PostDynamoMapper {

    private static Logger logger = LoggerFactory.getLogger(PostDynamoMapper.class);
    private static final String IMAGE_COLUMN = "image";
    private static final String INSTANT_COLUMN = "instant";
    private static final String REPORTS_COLUMN = "reportsNumber";
    private static final String IS_PUBLIC_COLUMN = "isPublic";

    private static final int IMG_WIDTH = 280;
    private static final int IMG_HEIGHT = 350;

    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");


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

    public Map<String, AttributeValue> toDynamoDbItem(Post post) {
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
                    } else {
                        item.put(field.getName(), AttributeValue.builder().s((String) field.get(post)).build());
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error(String.format("An exception occurred when accessing field %s of Post object by reflection!", field.getName()), e);
            } catch (IOException e) {
                logger.error("An exception occurred when resize image!", e);
            }
        });
        return item;
    }

    public Post toPost(Map<String, AttributeValue> item) {
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
