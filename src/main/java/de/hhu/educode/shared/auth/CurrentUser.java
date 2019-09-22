package de.hhu.educode.shared.auth;

import de.hhu.educode.shared.grpc.UserInterceptor;

public class CurrentUser {

    public static boolean hasUsername(String username) {
        var user = get();
        return user != null && user.id().equals(username);
    }

    public static boolean hasRole(Role role) {
        var user = get();
        return user != null && user.role().covers(role);
    }

    public static boolean hasExactRole(Role role) {
        var user = get();
        return user != null && user.role().equals(role);
    }

    public static boolean isPresent() {
        return get() != null;
    }

    public static User get() {
        return UserInterceptor.CONTEXT_KEY.get();
    }
}
