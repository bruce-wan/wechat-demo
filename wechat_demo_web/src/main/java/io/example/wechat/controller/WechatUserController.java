package io.example.wechat.controller;

import io.example.wechat.config.WechatProperties;
import io.example.wechat.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by bruce.wan on 2021/3/9.
 */
@Slf4j
@RestController
@RequestMapping("/wechat/user")
public class WechatUserController {

    private final WechatProperties wechatProperties;
    private final WechatService WechatService;

    public WechatUserController(WechatProperties wechatProperties, WechatService wechatService) {
        this.wechatProperties = wechatProperties;
        this.WechatService = wechatService;
    }

    @PostMapping("/login")
    public String login(@RequestParam String code) throws WxErrorException {
        return WechatService.login(code, wechatProperties.getEncryptKey());
    }
}
