package io.github.asleepyfish.exception;

import io.github.asleepyfish.enums.exception.ChatGPTErrorEnum;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-08 19:49
 * @Description: ChatGPTException
 */
public class ChatGPTException extends RuntimeException {
    private String errorCode = "-1";

    private String errorMessage = "";

    public ChatGPTException() {
        super();
    }

    public ChatGPTException(ChatGPTErrorEnum chatGPTErrorEnum) {
        super(chatGPTErrorEnum.getErrorMessage());
        this.errorCode = chatGPTErrorEnum.getErrorCode();
        this.errorMessage = chatGPTErrorEnum.getErrorMessage();
    }

    public ChatGPTException(ChatGPTErrorEnum chatGPTErrorEnum, Object... messages) {
        super(String.format(chatGPTErrorEnum.getErrorMessage(), messages));
        this.errorCode = chatGPTErrorEnum.getErrorCode();
        this.errorMessage = String.format(chatGPTErrorEnum.getErrorMessage(), messages);
    }

    public ChatGPTException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
