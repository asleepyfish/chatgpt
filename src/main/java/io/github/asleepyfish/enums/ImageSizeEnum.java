package io.github.asleepyfish.enums;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-14 00:39
 * @Description: ImageSizeEnum
 */
public enum ImageSizeEnum {
    /**
     * 256x256
     */
    S256x256("256x256"),
    S512x512("512x512"),
    S1024x1024("1024x1024");
    private final String size;

    ImageSizeEnum(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }
}
