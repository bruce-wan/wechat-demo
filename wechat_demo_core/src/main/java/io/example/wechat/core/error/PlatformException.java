package io.example.wechat.core.error;

/**
 * Created by bruce.wan on 2021/6/29.
 */
public class PlatformException extends RuntimeException {
    private static final long serialVersionUID = 9195186990525232821L;

    private String errorCode;

    public PlatformException(String errorCode) {
        this.errorCode = errorCode;
    }

    public PlatformException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PlatformException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public PlatformException(String errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public PlatformException(String errorCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
    }
}
