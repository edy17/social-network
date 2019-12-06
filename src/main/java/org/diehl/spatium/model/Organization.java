package org.diehl.spatium.model;


import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RegisterForReflection
public class Organization implements Serializable {

    private String name;
    private String userIdOfAdmin;
    private List<String> postIds;
    private List<String> userIdsOfMembers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserIdOfAdmin() {
        return userIdOfAdmin;
    }

    public void setUserIdOfAdmin(String userIdOfAdmin) {
        this.userIdOfAdmin = userIdOfAdmin;
    }

    public List<String> getPostIds() {
        if (postIds == null) {
            return new ArrayList<>();
        }
        return postIds;
    }

    public void setPostIds(List<String> postIds) {
        this.postIds = postIds;
    }

    public List<String> getUserIdsOfMembers() {
        if (userIdsOfMembers == null) {
            return new ArrayList<>();
        }
        return userIdsOfMembers;
    }

    public void setUserIdsOfMembers(List<String> userIdsOfMembers) {
        this.userIdsOfMembers = userIdsOfMembers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Organization)) return false;
        Organization that = (Organization) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
