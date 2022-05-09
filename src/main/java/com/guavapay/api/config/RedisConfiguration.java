package com.guavapay.api.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfiguration {

    @Value("${redis.url}")
    private String url;

    @Bean
    RedissonClient redissonSingleClient() {
        Config config = new Config();
        config.setCodec(new SerializationCodec());

        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(url);
        return Redisson.create(config);
    }
}
