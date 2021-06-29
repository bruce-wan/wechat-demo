package io.example.wechat.core.error;

import io.example.wechat.core.util.ErrorCode;

/**
 * Created by bruce.wan on 2021/3/12.
 */
public class CodecException extends PlatformException {

    public CodecException(String message) {
        this(ErrorCode.CODEC_ERROR, message);
    }

    public CodecException(String message, Throwable cause) {
        this(ErrorCode.CODEC_ERROR, message, cause);
    }

    public CodecException(String errorCode, String message) {
        super(errorCode, message);
    }

    public CodecException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
