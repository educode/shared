package de.hhu.educode.shared.grpc;

import de.hhu.educode.shared.reactor.ReactiveContext;
import io.grpc.*;
import lombok.Builder;
import lombok.Data;

public class TracingInterceptor implements ServerInterceptor, ClientInterceptor {

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
        var context = Context.current().withValue(CONTEXT_KEY, Trace.fromMetadata(headers));
        return Contexts.interceptCall(context, call, headers, next);
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                var metadataOptional = ReactiveContext.currentTrace()
                        .map(Trace::toMetadata)
                        .blockOptional();

                metadataOptional.ifPresent(headers::merge);
                super.start(responseListener, headers);
            }
        };
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

        public static Trace fromMetadata(Metadata metadata) {
            var builder = Trace.builder();
            var requestId = metadata.get(REQUEST_ID);
            if (requestId != null) { builder.requestId(requestId); }

            var traceId = metadata.get(TRACE_ID);
            if (traceId != null) { builder.traceId(traceId); }

            var spanId = metadata.get(SPAN_ID);
            if (spanId != null) { builder.spanId(spanId); }

            var parentSpanId = metadata.get(PARENT_SPAN_ID);
            if (parentSpanId != null) { builder.parentSpanId(parentSpanId); }

            var sampled = metadata.get(SAMPLED);
            if (sampled != null) { builder.sampled(sampled); }

            var flags = metadata.get(FLAGS);
            if (flags != null) { builder.flags(flags); }

            var spanContext = metadata.get(SPAN_CONTEXT);
            if (spanContext != null) { builder.spanContext(spanContext); }

            return builder.build();
        }

        public Metadata toMetadata() {
            var metadata = new Metadata();

            if (requestId != null) { metadata.put(REQUEST_ID, requestId); }
            if (traceId != null) { metadata.put(TRACE_ID, traceId); }
            if (spanId != null) { metadata.put(SPAN_ID, spanId); }
            if (parentSpanId != null) { metadata.put(PARENT_SPAN_ID, parentSpanId); }
            if (sampled != null) { metadata.put(SAMPLED, sampled); }
            if (flags != null) { metadata.put(FLAGS, flags); }
            if (spanContext != null) { metadata.put(SPAN_CONTEXT, spanContext); }

            return metadata;
        }
    }
}
