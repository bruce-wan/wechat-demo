package io.example.wechat.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaMessage;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import cn.binarywang.wx.miniapp.message.WxMaMessageRouter;
import io.example.wechat.core.model.UserData;
import io.example.wechat.core.util.SimpleEncrypt;
import io.example.wechat.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Created by bruce.wan on 2021/3/11.
 */
@Slf4j
@Service
public class WechatServiceImpl implements WechatService {

    private final WxMpService wxMpService;
    private final WxMaService wxMaService;

    private final WxMaMessageRouter wxMaMessageRouter;
    private final WxMpMessageRouter wxMpMessageRouter;

    public WechatServiceImpl(WxMpService wxMpService, WxMaService wxMaService, WxMaMessageRouter wxMaMessageRouter, WxMpMessageRouter wxMpMessageRouter) {
        this.wxMpService = wxMpService;
        this.wxMaService = wxMaService;
        this.wxMaMessageRouter = wxMaMessageRouter;
        this.wxMpMessageRouter = wxMpMessageRouter;
    }

    @Override
    public boolean checkSignature(String wxtype, String timestamp, String nonce, String signature) {
        boolean flag;
        switch (wxtype) {
            case "mp":
                flag = wxMpService.checkSignature(timestamp, nonce, signature);
                break;
            case "ma":
                flag = wxMaService.checkSignature(timestamp, nonce, signature);
                break;
            default:
                flag = false;
        }
        return flag;
    }

    @Override
    public String login(String code, String encryptKey) throws WxErrorException {
        WxMaUserService wxMaUserService = wxMaService.getUserService();
        WxMaJscode2SessionResult sessionResult = wxMaUserService.getSessionInfo(code);
        String sessionKey = sessionResult.getSessionKey();
        String encryptSessionKey = SimpleEncrypt.aesEncryptToBase64(sessionKey, encryptKey);

        UserData userData = new UserData();
        userData.setNickName("微信用户");
        userData.setOpenId(sessionResult.getOpenid());
        userData.setUnionId(sessionResult.getUnionid());
        userData.setSessionKey(sessionKey);
        userData.setEncryptSessionKey(encryptSessionKey);

        WxMaUserInfo wxMaUserInfo;
        WxMpUser wxMpUser;
        //TODO: save to database
        return encryptSessionKey;
    }

    @Override
    public String processMessage(String wxtype, String requestBody, String encryptType, String timestamp, String nonce, String msgSignature) {
        switch (wxtype) {
            case "mp":
                return processWxMpMessage(requestBody, encryptType, timestamp, nonce, msgSignature);
            case "ma":
                return processWxMaMessage(requestBody, encryptType, timestamp, nonce, msgSignature);
            default:
                throw new RuntimeException("not implements yet.");
        }
    }

    private String processWxMpMessage(String requestBody, String encryptType, String timestamp, String nonce, String msgSignature) {
        String outXml = "";
        if (StringUtils.isBlank(encryptType)) {
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
            if (outMessage != null) {
                outXml = outMessage.toXml();
            }
        } else {
            WxMpConfigStorage configStorage = wxMpService.getWxMpConfigStorage();
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, configStorage, timestamp, nonce, msgSignature);
            log.debug("\nDecrypted message: \n{} ", inMessage.toString());
            WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
            if (outMessage != null) {
                outXml = outMessage.toEncryptedXml(configStorage);
            }
        }
        log.debug("\nResponse message: {}", outXml);
        return outXml;
    }

    private String processWxMaMessage(String requestBody, String encryptType, String timestamp, String nonce, String msgSignature) {
        WxMaConfig wxMaConfig = wxMaService.getWxMaConfig();
        boolean isJson = Objects.equals(wxMaConfig.getMsgDataFormat(), WxMaConstants.MsgDataFormat.JSON);

        WxMaMessage inMessage;

        if (StringUtils.isBlank(encryptType)) {
            if (isJson) {
                inMessage = WxMaMessage.fromJson(requestBody);
            } else {
                inMessage = WxMaMessage.fromXml(requestBody);
            }
        } else {
            if (isJson) {
                inMessage = WxMaMessage.fromEncryptedJson(requestBody, wxMaConfig);
            } else {
                inMessage = WxMaMessage.fromEncryptedXml(requestBody, wxMaConfig, timestamp, nonce, msgSignature);
            }
        }

        wxMaMessageRouter.route(inMessage);
        return "success";
    }
}
