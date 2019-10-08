package de.hhu.educode.shared.reactor;

import de.hhu.educode.shared.auth.User;
import de.hhu.educode.shared.grpc.TracingInterceptor;
import de.hhu.educode.shared.grpc.UserInterceptor;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import static de.hhu.educode.shared.grpc.TracingInterceptor.Trace;

public class ReactiveContext {

    public static final Class<User> USER_KEY = User.class;
    public static final Class<Trace> TRACE_KEY = Trace.class;

    public static Context current() {
        final var user = UserInterceptor.CONTEXT_KEY.get();
        final var trace = TracingInterceptor.CONTEXT_KEY.get();

        if (user != null) {
            return Context.of(USER_KEY, user, TRACE_KEY, trace);
        } else {
            return Context.of(TRACE_KEY, trace);
        }
    }

    public static Mono<User> currentUser() {
        return Mono.subscriberContext()
                .filter(ctx -> ctx.hasKey(USER_KEY))
                .map(ctx -> ctx.get(USER_KEY));
    }

    public static Mono<Trace> currentTrace() {
        return Mono.subscriberContext()
                .filter(ctx -> ctx.hasKey(TRACE_KEY))
                .map(ctx -> ctx.get(TRACE_KEY));
    }
}
