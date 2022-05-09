package com.guavapay.api.config;

import com.guavapay.api.client.KeyCloakClient;
import com.guavapay.api.util.CommonUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class TokenStorage {

    @Value("${keycloak.public-key}")
    private String publicKey;
    @Value("${keycloak.scope}")
    private String scope;
    @Value("${keycloak.grant-type}")
    private String grantType;

    private final KeyCloakClient keyCloakClient;
    private final RedissonClient redissonClient;

    public TokenStorage(KeyCloakClient keyCloakClient,
                        RedissonClient redissonClient) {
        this.keyCloakClient = keyCloakClient;
        this.redissonClient = redissonClient;
    }

    public String checkAndGetToken(String clientId, String secret) {
        String token = readToken("accessToken", clientId);
        if (isTokenTerminated(token)) return generateToken(clientId, secret);
        return token;
    }

    private String generateToken(String clientId, String secret) {
        Map<String, Object> form = new HashMap<>();
        form.put("scope", scope);
        form.put("client_id", clientId);
        form.put("client_secret", secret);
        form.put("grant_type", grantType);

        String token = String.valueOf(keyCloakClient.getToken(form).get("access_token"));
        saveKeyCloakToken("accessToken", clientId, token, 1);
        return token;
    }

    private boolean isTokenTerminated(String token) {
        if (Objects.isNull(token)) return true;
        try {
            Jwts.parser().setSigningKey(CommonUtil.getPublicKeyFromString(publicKey)).parseClaimsJws(token).getBody();
            return false;
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveKeyCloakToken(String prefix, String clientId, String value, long lifeTime) {
        RBucket<String> bucket = redissonClient.getBucket(String.format("%s:%s", prefix, clientId));
        if (Objects.nonNull(bucket)) {
            bucket.delete();
        }
        return !bucket.trySet(value, lifeTime, TimeUnit.DAYS);
    }

    public String readToken(String prefix, String clientId) {
        RBucket<String> bucket = redissonClient.getBucket(String.format("%s:%s", prefix, clientId));
        return bucket.get();
    }
}
