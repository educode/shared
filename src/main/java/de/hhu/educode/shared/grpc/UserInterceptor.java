package de.hhu.educode.shared.grpc;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.hhu.educode.shared.auth.Claims;
import de.hhu.educode.shared.auth.Role;
import de.hhu.educode.shared.auth.User;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.regex.Pattern;

@Slf4j
public class UserInterceptor implements ServerInterceptor {

    public static final Context.Key<User> CONTEXT_KEY = Context.key("de.hhu.educode.user");
    private static final Metadata.Key<String> AUTHORIZATION_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private static final String HEADER_PREFIX = "Bearer ";
    private static final Pattern HEADER_PATTERN = Pattern.compile(HEADER_PREFIX);

    private static final int EXPECTED_HEADER_PARTS = 2;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        var authHeader = headers.get(AUTHORIZATION_KEY);
        if (authHeader == null || !authHeader.startsWith(HEADER_PREFIX)) {
            log.debug("Authentication header not present or invalid");
            return next.startCall(call, headers);
        }

        var headerParts = HEADER_PATTERN.split(authHeader);
        if (headerParts.length != EXPECTED_HEADER_PARTS) {
            log.debug("Authentication header invalid");
            return next.startCall(call, headers);
        }

        try {
            var token = headerParts[1];
            var user = getUser(SignedJWT.parse(token).getJWTClaimsSet());
            var context = Context.current().withValue(CONTEXT_KEY, user);
            return Contexts.interceptCall(context, call, headers, next);
        } catch (ParseException ignored) {
            log.debug("Parsing authentication token failed");
            return next.startCall(call, headers);
        }
    }

    private static User getUser(JWTClaimsSet claimsSet) throws ParseException {
        return User.builder()
                .id(claimsSet.getStringClaim(Claims.USERNAME))
                .email(claimsSet.getStringClaim(Claims.EMAIL))
                .title(claimsSet.getStringClaim(Claims.TITLE))
                .firstname(claimsSet.getStringClaim(Claims.FIRSTNAME))
                .lastname(claimsSet.getStringClaim(Claims.LASTNAME))
                .role(Role.from(claimsSet.getStringClaim(Claims.ROLE)))
                .build();
    }
}
