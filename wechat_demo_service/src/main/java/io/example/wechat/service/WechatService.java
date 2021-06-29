package io.example.wechat.service;

import me.chanjar.weixin.common.error.WxErrorException;

/**
 * Created by bruce.wan on 2021/6/23.
 */
public interface WechatService {

    boolean checkSignature(String wxtype, String timestamp, String nonce, String signature);

    String login(String code, String encryptKey) throws WxErrorException;

    String processMessage(String wxtype, String requestBody, String encryptType, String timestamp, String nonce, String msgSignature);
}

