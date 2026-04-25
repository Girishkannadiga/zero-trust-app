package com.zerotrust.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class DeviceNotTrustedException extends RuntimeException {
    public DeviceNotTrustedException(String message) {
        super(message);
    }
}
