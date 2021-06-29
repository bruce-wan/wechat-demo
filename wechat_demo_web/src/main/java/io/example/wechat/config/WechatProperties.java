package io.example.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by bruce.wan on 2021/6/23.
 */
@Data
@ConfigurationProperties(prefix = WechatProperties.PREFIX)
public class WechatProperties {
    public static final String PREFIX = "wechat.configs";

    private String encryptKey;
    private Config mp;
    private Config miniapp;

    @Data
    public static class Config {
        private String appid;
        private String secret;
        private String token;
        private String aesKey;
        private String msgDataFormat;
    }
}
