package de.hhu.educode.shared.grpc;

import io.grpc.*;
import lombok.Builder;
import lombok.Data;

public class TracingInterceptor implements ServerInterceptor {

    public static final Context.Key<Trace> CONTEXT_KEY = Context.key("de.hhu.educode.trace");

    private static final Metadata.Key<String> REQUEST_ID = Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> TRACE_ID = Metadata.Key.of("x-b3-traceid", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> SPAN_ID = Metadata.Key.of("x-b3-traceid", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> PARENT_SPAN_ID = Metadata.Key.of("x-b3-parentspanid", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> SAMPLED = Metadata.Key.of("x-b3-sampled", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> FLAGS = Metadata.Key.of("x-b3-flags", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> SPAN_CONTEXT = Metadata.Key.of("x-ot-span-context", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        var builder = Trace.builder();
        var requestId = headers.get(REQUEST_ID);
        if (requestId != null) {
            builder.requestId(requestId);
        }

        var traceId = headers.get(TRACE_ID);
        if (traceId != null) {
            builder.traceId(traceId);
        }

        var spanId = headers.get(SPAN_ID);
        if (spanId != null) {
            builder.spanId(spanId);
        }

        var parentSpanId = headers.get(PARENT_SPAN_ID);
        if (parentSpanId != null) {
            builder.parentSpanId(parentSpanId);
        }

        var sampled = headers.get(SAMPLED);
        if (sampled != null) {
            builder.sampled(sampled);
        }

        var flags = headers.get(FLAGS);
        if (flags != null) {
            builder.flags(flags);
        }

        var spanContext = headers.get(SPAN_CONTEXT);
        if (spanContext != null) {
            builder.spanContext(spanContext);
        }

        var context = Context.current().withValue(CONTEXT_KEY, builder.build());
        return Contexts.interceptCall(context, call, headers, next);
    }

    @Data
    @Builder
    public static final class Trace {
        private final String requestId;
        private final String traceId;
        private final String spanId;
        private final String parentSpanId;
        private final String sampled;
        private final String flags;
        private final String spanContext;
    }
}
