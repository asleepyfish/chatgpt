package io.github.asleepyfish.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-14 00:25
 * @Description: ImageResponseFormatEnum
 */
@Getter
@AllArgsConstructor
public enum ImageResponseFormatEnum {
    /**
     * url
     */
    URL("url"),
    B64_JSON("b64_json");
    private final String responseFormat;
}
