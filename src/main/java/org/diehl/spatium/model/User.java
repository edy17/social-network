package org.diehl.spatium.model;


import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RegisterForReflection
public class User extends AbstractBaseEntity {

    private static Logger logger = Logger.getLogger("org.diehl.spatium.model.User");

    private String username;
    private String password;

    public static User from(Map<String, AttributeValue> item) {
        User user = new User();
        if (item != null && !item.isEmpty()) {
            Arrays.stream(User.class.getDeclaredFields()).forEach(field -> {
                try {
                    field.setAccessible(true);
                    field.set(user, item.get(field.getName()).s());

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
}
