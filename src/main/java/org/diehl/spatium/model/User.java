package org.diehl.spatium.model;


import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@RegisterForReflection
public class User extends AbstractBaseEntity {

    private static Logger logger = Logger.getLogger("org.diehl.spatium.model.User");

    private String username;
    private String password;
    private String email;

    public static User from(Map<String, AttributeValue> item) {
        User user = new User();
        if (item != null && !item.isEmpty()) {
            Stream.of(AbstractBaseEntity.class.getDeclaredFields(), User.class.getDeclaredFields()).flatMap(Stream::of).forEach(field -> {
                try {
                    if ((!field.getName().equals("logger"))
                    && item.containsKey(field.getName())) {
                        field.setAccessible(true);
                        field.set(user, item.get(field.getName()).s());
                    }
                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE, "An exception was thrown: ", e);
                }
            });
        }
        return user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
