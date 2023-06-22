package io.github.asleepyfish.enums.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-08 19:50
 * @Description: ChatGPTErrorEnum
 */
@Getter
@AllArgsConstructor
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
    FAILED_TO_GENERATE_IMAGE("10003", "generate image error, reason is %s."),

    /**
     * ERROR_RESPONSE_FORMAT
     */
    ERROR_RESPONSE_FORMAT("10004", "please check if the response_format is b64_json!"),

    /**
     * DOWNLOAD_IMAGE_ERROR
     */
    DOWNLOAD_IMAGE_ERROR("10005", "failed to download image!"),

    /**
     * QUERY_BILLINGUSAGE_ERROR
     */
    QUERY_BILLINGUSAGE_ERROR("10006", "query billingUsage error, reason is %s."),

    /**
     * EDIT_ERROR
     */
    EDIT_ERROR("10007", "edit error, reason is %s."),

    /**
     * EMBEDDINGS_ERROR
     */
    EMBEDDINGS_ERROR("10008", "embeddings error, reason is %s."),

    /**
     * TRANSCRIPTION_ERROR
     */
    TRANSCRIPTION_ERROR("10009", "transcription error, reason is %s."),

    /**
     * TRANSLATION_ERROR
     */
    TRANSLATION_ERROR("10010", "translation error, reason is %s.");


    private final String errorCode;
    private final String errorMessage;
}
