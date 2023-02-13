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
    FAILED_TO_GENERATE_ANSWER("10001", "generate answer error, reason is %s."),

    /**
     * MODEL_SELECTION_ERROR
     */
    MODEL_SELECTION_ERROR("10002", "there is no such model!"),

    /**
     * FAILED_TO_GENERATE_IMAGE
     */
    FAILED_TO_GENERATE_IMAGE("10003", "generate image error, reason is %s.");
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
