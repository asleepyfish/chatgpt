package io.github.asleepyfish.enums.audio;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: asleepyfish
 * @Date: 2023/6/22 15:55
 * @Description: AudioResponseFormatEnum
 */
@Getter
@AllArgsConstructor
public enum AudioResponseFormatEnum {

    /**
     * JSON
     */
    JSON("json"),

    /**
     * TEXT
     */
    TEXT("text"),

    /**
     * SRT
     */
    SRT("srt"),

    /**
     * VERBOSE_JSON
     */
    VERBOSE_JSON("verbose_json"),

    /**
     * VTT
     */
    VTT("vtt");


    private final String format;
}
