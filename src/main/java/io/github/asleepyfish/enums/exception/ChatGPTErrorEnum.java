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
    TRANSLATION_ERROR("10010", "translation error, reason is %s."),

    /**
     * CREATE_IMAGE_EDIT_ERROR
     */
    CREATE_IMAGE_EDIT_ERROR("10011", "create image edit error, reason is %s."),

    /**
     * CREATE_IMAGE_VARIATION_ERROR
     */
    CREATE_IMAGE_VARIATION_ERROR("10012", "create image variation error, reason is %s."),

    /**
     * LIST_FILES_ERROR
     */
    LIST_FILES_ERROR("10013", "list files error, reason is %s."),

    /**
     * UPLOAD_FILE_ERROR
     */
    UPLOAD_FILE_ERROR("10014", "upload file error, reason is %s."),

    /**
     * DELETE_FILE_ERROR
     */
    DELETE_FILE_ERROR("10015", "delete file error, reason is %s."),

    /**
     * RETRIEVE_FILE_ERROR
     */
    RETRIEVE_FILE_ERROR("10016", "retrieve file error, reason is %s."),

    /**
     * RETRIEVE_FILE_CONTENT_ERROR
     */
    RETRIEVE_FILE_CONTENT_ERROR("10017", "retrieve file content error, reason is %s."),

    /**
     * CREATE_FINE_TUNE_ERROR
     */
    CREATE_FINE_TUNE_ERROR("10018", "create fine tune error, reason is %s."),

    /**
     * LIST_FINE_TUNES_ERROR
     */
    LIST_FINE_TUNES_ERROR("10019", "list fine tunes error, reason is %s."),

    /**
     * CREATE_FINE_TUNE_COMPLETION_ERROR
     */
    CREATE_FINE_TUNE_COMPLETION_ERROR("10020", "create fine tune completion error, reason is %s."),

    /**
     * RETRIEVE_FINE_TUNE_ERROR
     */
    RETRIEVE_FINE_TUNE_ERROR("10021", "retrieve fine tune error, reason is %s."),

    /**
     * CANCEL_FINE_TUNE_ERROR
     */
    CANCEL_FINE_TUNE_ERROR("10022", "cancel fine tune error, reason is %s."),

    /**
     * LIST_FINE_TUNE_EVENTS_ERROR
     */
    LIST_FINE_TUNE_EVENTS_ERROR("10023", "list fine tune events error, reason is %s."),

    /**
     * DELETE_FINE_TUNE_ERROR
     */
    DELETE_FINE_TUNE_ERROR("10024", "delete fine tune error, reason is %s."),

    /**
     * CREATE_MODERATION_ERROR
     */
    CREATE_MODERATION_ERROR("10025", "create moderation error, reason is %s."),

    /**
     * SSL_CONTEXT_INIT_ERROR
     */
    SSL_CONTEXT_INIT_ERROR("10026", "ssl context init error, reason is %s."),

    /**
     * base Url must end with /
     */
    BASE_URL_MUST_END_WITH_SLASH("10027", "baseUrl [%s] must end with /"),

    /**
     * no available token, please check token configuration
     */
    NO_AVAILABLE_TOKEN_ERROR("10028", "no available token, please check token configuration"),

    /**
     * token is invalid, please check token configuration
     */
    TOKEN_IS_INVALID_ERROR("10029", "token is invalid, please check token configuration, reason is [%s].");

    private final String errorCode;

    private final String errorMessage;
}
