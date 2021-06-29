package io.example.wechat.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.binarywang.wx.miniapp.message.WxMaMessageRouter;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by bruce.wan on 2021/2/26.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(WechatProperties.class)
public class WechatConfig {

    private final WechatProperties wechatProperties;

    public WechatConfig(WechatProperties wechatProperties) {
        this.wechatProperties = wechatProperties;
    }

    @Bean
    public WxMaService getWxMaService() {
        WechatProperties.Config config = wechatProperties.getMiniapp();
        if (config == null) {
            throw new RuntimeException("读取微信小程序配置失败");
        }

        WxMaDefaultConfigImpl wxMaConfig = new WxMaDefaultConfigImpl();
        wxMaConfig.setAppid(config.getAppid());
        wxMaConfig.setSecret(config.getSecret());
        wxMaConfig.setToken(config.getToken());
        wxMaConfig.setAesKey(config.getAesKey());
        wxMaConfig.setMsgDataFormat(config.getMsgDataFormat());

        WxMaService wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(wxMaConfig);

        return wxMaService;
    }

    @Bean
    public WxMaMessageRouter getWxMaMessageRouter(WxMaService wxMaService) {
        WxMaMessageRouter messageRouter = new WxMaMessageRouter(wxMaService);
        // message log hander
        messageRouter.rule().handler((message, map, service, wxSessionManager) -> {
            log.info("\n接收到请求消息，内容：{}", message);
            return null;
        }).next();
        return messageRouter;
    }

    @Bean
    public WxMpService wxMpService() {
        WechatProperties.Config config = wechatProperties.getMp();
        if (config == null) {
            throw new RuntimeException("读取微信公众号配置失败");
        }

        WxMpServiceImpl wxMpService = new WxMpServiceImpl();
        WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
        configStorage.setAppId(config.getAppid());
        configStorage.setSecret(config.getSecret());
        configStorage.setToken(config.getToken());
        configStorage.setAesKey(config.getAesKey());
        wxMpService.setWxMpConfigStorage(configStorage);

        return wxMpService;
    }

    @Bean
    public WxMpMessageRouter getWxMpMessageRouter(WxMpService wxMpService) {
        WxMpMessageRouter messageRouter = new WxMpMessageRouter(wxMpService);
        // message log hander
        messageRouter.rule().handler((message, map, service, wxSessionManager) -> {
            log.info("\n接收到请求消息，内容：{}", message);
            return null;
        }).next();
        return messageRouter;
    }
}
