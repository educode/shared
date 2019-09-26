package de.hhu.educode.shared.reactor;

import de.hhu.educode.shared.auth.Role;
import de.hhu.educode.shared.auth.User;
import de.hhu.educode.shared.grpc.UserInterceptor;
import io.grpc.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

public class ReactiveUserHolder {

    private static final Class<?> USER_KEY = User.class;

    public static Mono<User> currentUser() {
        return Mono.subscriberContext()
                .filter(ctx -> ctx.hasKey(USER_KEY))
                .map(ctx -> ctx.<User>get(USER_KEY));
    }

    public static Context currentUserContext() {
        return Context.of(USER_KEY, UserInterceptor.CONTEXT_KEY.get());
    }

    public static Function<Context, Context> clearContext() {
        return context -> context.delete(USER_KEY);
    }

    public static Flux<?> withCurrentUser(Flux<?> flux) {
        return flux.subscriberContext(currentUserContext());
    }

    public static Mono<?> withCurrentUser(Mono<?> mono) {
        return mono.subscriberContext(currentUserContext());
    }

    public static <T> Mono<T> assertRole(Role role, Mono<T> mono) {
        return currentUser()
                .switchIfEmpty(Mono.error(Status.UNAUTHENTICATED.asException()))
                .filter(user -> user.hasRole(role))
                .switchIfEmpty(Mono.error(Status.PERMISSION_DENIED.asException()))
                .then(mono);
    }

    public static <T> Mono<T> assertExactRole(Role role, Mono<T> mono) {
        return currentUser()
                .switchIfEmpty(Mono.error(Status.UNAUTHENTICATED.asException()))
                .filter(user -> user.hasExactRole(role))
                .switchIfEmpty(Mono.error(Status.PERMISSION_DENIED.asException()))
                .then(mono);
    }

    public static <T> Flux<T> assertRole(Role role, Flux<T> flux) {
        return currentUser()
                .switchIfEmpty(Mono.error(Status.UNAUTHENTICATED.asException()))
                .filter(user -> user.hasRole(role))
                .switchIfEmpty(Mono.error(Status.PERMISSION_DENIED.asException()))
                .thenMany(flux);
    }

    public static <T> Flux<T> assertExactRole(Role role, Flux<T> flux) {
        return currentUser()
                .switchIfEmpty(Mono.error(Status.UNAUTHENTICATED.asException()))
                .filter(user -> user.hasExactRole(role))
                .switchIfEmpty(Mono.error(Status.PERMISSION_DENIED.asException()))
                .thenMany(flux);
    }
}
