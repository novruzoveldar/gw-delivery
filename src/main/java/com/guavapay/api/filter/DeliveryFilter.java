package com.guavapay.api.filter;

import com.guavapay.api.config.TokenStorage;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.naming.AuthenticationException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

@Component
public class DeliveryFilter implements GlobalFilter {

    private static final String authPath = "/base/auth/**";
    private static final String CLIENT_DATA = "X-Client-Data";
    private static final String CREDENTIALS_SPLIT_SIGN = ":";

    private final TokenStorage tokenStorage;

    public DeliveryFilter(TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var response = exchange.getResponse();
        if (authPath.contains(request.getURI().getPath())) {
            try {
                response.setStatusCode(HttpStatus.OK);
                byte[] bytes = fillPrincipal(request).getBytes(StandardCharsets.UTF_8);

                DataBuffer buffer = response.bufferFactory().wrap(bytes);
                return response.writeWith(Mono.just(buffer));
            } catch (AuthenticationException e) {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            }
        }
        return chain.filter(exchange.mutate().request(request).build());
    }

    public String fillPrincipal(ServerHttpRequest request) throws AuthenticationException {
        String[] credentials = new String[0];
        String clientAuth = request.getHeaders().getFirst(CLIENT_DATA);
        if (Objects.nonNull(clientAuth)) {
            credentials = new String(Base64.getDecoder().decode(clientAuth)).split(CREDENTIALS_SPLIT_SIGN);
            if (credentials.length != 2)
                throw new AuthenticationException("Invalid credentials");
        }
        String token = tokenStorage.checkAndGetToken(credentials[0], credentials[1]);
        if (Objects.nonNull(token)) return token;
        throw new AuthenticationException("Invalid credentials");
    }

}
