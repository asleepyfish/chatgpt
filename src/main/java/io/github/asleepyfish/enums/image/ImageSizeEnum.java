package io.github.asleepyfish.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-14 00:39
 * @Description: ImageSizeEnum
 */
@Getter
@AllArgsConstructor
public enum ImageSizeEnum {

    /**
     * 256x256
     */
    S256x256("256x256"),
    S512x512("512x512"),
    S1024x1024("1024x1024");
    private final String size;
}
