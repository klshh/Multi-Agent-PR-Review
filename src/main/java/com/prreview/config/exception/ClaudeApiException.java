package com.prreview.config.exception;

/** Thrown whenever the Claude API call fails or returns something we can't parse. */
public class ClaudeApiException extends RuntimeException {
    public ClaudeApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClaudeApiException(String message) {
        super(message);
    }
}
