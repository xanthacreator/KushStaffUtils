package me.dankofuk.utils;

public class CodeData {
    private final long userId;
    private final String code;
    private final long expiryTime;

    public CodeData(long userId, String code, long expiryTime) {
        this.userId = userId;
        this.code = code;
        this.expiryTime = expiryTime;
    }

    public long getUserId() {
        return userId;
    }

    public String getCode() {
        return code;
    }

    public long getExpiryTime() {
        return expiryTime;
    }
}


