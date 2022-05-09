package com.guavapay.api.config;

import com.guavapay.api.util.CommonUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CourierModuleFilter extends AbstractGatewayFilterFactory<CourierModuleFilter.Config> {

    private static final String CLIENT_TOKEN = "Client_Token";

    @Value("${keycloak.clientCourier.id}")
    private String clientCourier;

    @Value("${keycloak.public-key}")
    private String publicKey;

    public CourierModuleFilter() {
        super(CourierModuleFilter.Config.class);
    }

    @Data
    @NoArgsConstructor
    protected static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }

    @Override
    public GatewayFilter apply(CourierModuleFilter.Config config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            var response = exchange.getResponse();

            String clientAuthToken = request.getHeaders().getFirst(CLIENT_TOKEN);
            if (Objects.isNull(clientAuthToken)) {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            }
            Claims claims = null;
            try {
                claims = Jwts.parser().setSigningKey(CommonUtil.getPublicKeyFromString(publicKey)).parseClaimsJws(clientAuthToken).getBody();
            } catch (ExpiredJwtException ex) {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            assert claims != null;
            String clientId = claims.get("clientId").toString();
            if (Objects.equals(clientId, clientCourier))
                return chain.filter(exchange.mutate().request(request).build());
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        };
    }
}
