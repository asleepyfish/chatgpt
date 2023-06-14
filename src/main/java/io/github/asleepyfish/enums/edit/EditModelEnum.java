package io.github.asleepyfish.enums.edit;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: asleepyfish
 * @Date: 2023/6/14 19:25
 * @Description: EditModelEnum
 */
@Getter
@AllArgsConstructor
public enum EditModelEnum {
    /**
     * text_davinci_edit_001
     */
    TEXT_DAVINCI_EDIT_001("text_davinci_edit_001"),

    /**
     * code_davinci_edit_001
     */
    CODE_DAVINCI_EDIT_001("code-davinci-edit-001");

    /**
     * modelName
     */
    private final String modelName;
}
