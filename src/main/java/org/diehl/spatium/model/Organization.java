package org.diehl.spatium.model;


import java.util.Collection;

public class Organization extends AbstractBaseEntity {
    private Long id;
    private String name;
    private String photo;
    private boolean isAnimal;
    private Collection<Post> posts;
    private Collection<User> users;
}
