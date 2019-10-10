package de.hhu.educode.shared.reactor;

import de.hhu.educode.shared.auth.User;
import org.slf4j.MDC;
import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;

import java.util.function.Consumer;

import static de.hhu.educode.shared.grpc.TracingInterceptor.Trace;
import static de.hhu.educode.shared.reactor.ReactiveContext.TRACE_KEY;
import static de.hhu.educode.shared.reactor.ReactiveContext.USER_KEY;

public class ReactiveLogger {

    private static final String USER_LOG_KEY = "user-id";
    private static final String REQ_LOG_KEY = "req-id";

    public static <T> Consumer<Signal<T>> logOnError(Consumer<Throwable> logStatement) {
        return signal -> {
            if (signal.getType() != SignalType.ON_ERROR) {
                return;
            }

            var context = signal.getContext();
            var traceOptional = context.<Trace>getOrEmpty(TRACE_KEY);
            var userOptional = context.<User>getOrEmpty(USER_KEY);

            try {
                traceOptional.ifPresent(trace -> MDC.put(REQ_LOG_KEY, trace.getRequestId()));
                userOptional.ifPresent(user -> MDC.put(USER_LOG_KEY, user.id()));
                logStatement.accept(signal.getThrowable());
            } finally {
                MDC.remove(REQ_LOG_KEY);
                MDC.remove(USER_LOG_KEY);
            }
        };
    }

    public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
        return signal -> {
            if (signal.getType() != SignalType.ON_NEXT) {
                return;
            }

            var context = signal.getContext();
            var traceOptional = context.<Trace>getOrEmpty(TRACE_KEY);
            var userOptional = context.<User>getOrEmpty(USER_KEY);

            try {
                traceOptional.ifPresent(trace -> MDC.put(REQ_LOG_KEY, trace.getRequestId()));
                userOptional.ifPresent(user -> MDC.put(USER_LOG_KEY, user.id()));
                logStatement.accept(signal.get());
            } finally {
                MDC.remove(REQ_LOG_KEY);
                MDC.remove(USER_LOG_KEY);
            }
        };
    }
}
