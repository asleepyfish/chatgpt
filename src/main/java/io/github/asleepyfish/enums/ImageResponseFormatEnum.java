package io.github.asleepyfish.enums;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-14 00:25
 * @Description: ImageResponseFormatEnum
 */
public enum ImageResponseFormatEnum {
    /**
     * url
     */
    URL("url"),
    B64_JSON("b64_json");
    private final String responseFormat;

    ImageResponseFormatEnum(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public String getResponseFormat() {
        return responseFormat;
    }
}
