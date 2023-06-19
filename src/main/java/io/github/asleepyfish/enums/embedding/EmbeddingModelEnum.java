package io.github.asleepyfish.enums.embedding;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: asleepyfish
 * @Date: 2023-06-19 20:58
 * @Description: EmbeddingModelEnum
 */
@Getter
@AllArgsConstructor
public enum EmbeddingModelEnum {
    /**
     * text-embedding-ada-002
     */
    TEXT_EMBEDDING_ADA_002("text-embedding-ada-002"),;

    /**
     * modelName
     */
    private final String modelName;
}
