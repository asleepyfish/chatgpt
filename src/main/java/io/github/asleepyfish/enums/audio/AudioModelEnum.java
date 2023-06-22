package io.github.asleepyfish.enums.audio;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: asleepyfish
 * @Date: 2023/6/22 9:56
 * @Description: AudioModelEnum
 */
@Getter
@AllArgsConstructor
public enum AudioModelEnum {

    /**
     * Only whisper-1 is currently available.
     */
    WHISPER_1("whisper-1");

    /**
     * modelName
     */
    private final String modelName;
}
