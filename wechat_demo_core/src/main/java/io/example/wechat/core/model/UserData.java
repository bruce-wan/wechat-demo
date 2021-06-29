package io.example.wechat.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by bruce.wan on 2021/6/23.
 */
@Data
public class UserData {
    @JsonProperty("openId")
    private String openId;
    @JsonProperty("nickName")
    private String nickName;
    private String gender;
    private String language;
    private String city;
    private String province;
    private String country;
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    @JsonProperty("unionId")
    private String unionId;
    @JsonProperty("sessionKey")
    private String sessionKey;
    @JsonProperty("encryptSessionKey")
    private String encryptSessionKey;
}
