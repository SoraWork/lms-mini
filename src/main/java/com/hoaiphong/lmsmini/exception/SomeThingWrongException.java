package com.hoaiphong.lmsmini.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)  // Tự động set status 400 cho business errors
public class SomeThingWrongException extends RuntimeException {
    private final String messageKey;  // Lưu key thay vì message cứng

    public SomeThingWrongException(String messageKey) {
        super(messageKey);  // Fallback: dùng key làm message nếu không resolve được
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}