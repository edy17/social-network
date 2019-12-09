package org.diehl.spatium.domain.model;


import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RegisterForReflection
public class Organization {

    @NotNull(message = "Please give a name to your organization")
    private String name;
    @NotNull(message = "Please give id of admin")
    private String userIdOfAdmin;
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
