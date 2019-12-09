package org.diehl.spatium.domain.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@RegisterForReflection
public class User {

    private String id;
    @Size(max = 20, min = 3, message = "{user.username.invalid}")
    @NotNull(message = "Please enter username")
    private String username;
    @Size(max = 20, min = 3, message = "{user.password.invalid}")
    @NotNull(message = "Please enter password")
    @JsonIgnore
    private String password;
    @Email(message = "{user.email.invalid}")
    @NotNull(message = "Please enter email")
    private String email;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User that = (User) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
