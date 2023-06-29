package io.github.asleepyfish.entity.audio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.io.File;

/**
 * @Author: asleepyfish
 * @Date: 2023/6/22 14:43
 * @Description: Translates audio into English.
 */
@Data
@Builder
public class TranslationRequest {

    /**
     * file
     * The audio file object (not file name) to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
     */
    private File file;

    /**
     * model
     * ID of the model to use. Only whisper-1 is currently available.
     */
    @NonNull
    private String model;

    /**
     * prompt
     * An optional text to guide the model's style or continue a previous audio segment. The prompt should match the audio language.
     */
    private String prompt;

    /**
     * responseFormat (Defaults to json)
     * The format of the transcript output, in one of these options: json, text, srt, verbose_json, or vtt.
     */
    @JsonProperty("response_format")
    private String responseFormat;

    /**
     * temperature (Defaults to 0)
     * The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will use log probability to automatically increase the temperature until certain thresholds are hit.
     */
    private Double temperature;
}
