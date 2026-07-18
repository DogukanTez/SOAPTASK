package com.dogukantez.soaptask.exception;

import java.util.Arrays;

public enum ViesFaultCode {

    INVALID_INPUT(false),
    SERVICE_UNAVAILABLE(true),
    MS_UNAVAILABLE(true),
    TIMEOUT(true),
    SERVER_BUSY(true),
    MS_MAX_CONCURRENT_REQ(true),
    GLOBAL_MAX_CONCURRENT_REQ(true),
    UNKNOWN(false);

    private final boolean temporary;

    ViesFaultCode(boolean temporary) {
        this.temporary = temporary;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public static ViesFaultCode from(String faultString) {
        if (faultString == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(code -> faultString.contains(code.name()))
                .findFirst()
                .orElse(UNKNOWN);
    }
}