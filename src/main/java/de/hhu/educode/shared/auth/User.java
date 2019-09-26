package de.hhu.educode.shared.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Builder
@Accessors(fluent = true)
@Getter
public class User {
    private final String id;
    private final String title;
    private final String firstname;
    private final String lastname;
    private final String email;
    private final Role role;

    public boolean hasRole(Role role) {
        return this.role.covers(role);
    }

    public boolean hasExactRole(Role role) {
        return this.role == role;
    }
}
