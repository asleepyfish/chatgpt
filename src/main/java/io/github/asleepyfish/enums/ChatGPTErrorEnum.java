package io.github.asleepyfish.enums;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-08 19:50
 * @Description: ChatGPTErrorEnum
 */
public enum ChatGPTErrorEnum {
    /**
     * FAILED_TO_GENERATE_ANSWER
     */
    FAILED_TO_GENERATE_ANSWER("10001", "generate answer error, the prompt is %s, reason is %s."),

    /**
     * MODEL_SELECTION_ERROR
     */
    MODEL_SELECTION_ERROR("10002", "there is no such model!");
    private final String errorCode;
    private final String errorMessage;

    ChatGPTErrorEnum(String errorCode, String errorMessage) {
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
