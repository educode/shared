package de.hhu.educode.shared.auth;

import java.util.stream.Stream;

public enum Role {
    ADMIN("admin", 900),
    TUTOR("tutor", 600),
    STUDENT("student", 300),
    GUEST("guest", 100);

    private final String value;
    private final int level;

    Role(String value, int level) {
        this.value = value;
        this.level = level;
    }

    public static Role from(String value) {
        return Stream.of(values())
                .filter(it -> it.toString().equals(value))
                .findFirst()
                .orElse(GUEST);
    }

    @Override
    public String toString() {
        return value;
    }

    public final boolean covers(Role other) {
        return this.level >= other.level;
    }
}
