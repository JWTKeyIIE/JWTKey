package com.xu.environmentInit.projectParser;

import com.xu.environmentInit.Exception.ExceptionHandler;

import java.util.List;
import java.util.Map;

public interface BuildFileParser {
    /**
     * getDependencyList.
     *
     * @return a {@link Map} object.
     * @throws ExceptionHandler if any.
     */
    Map<String, List<String>> getDependencyList() throws ExceptionHandler;

    /**
     * getProjectName.
     *
     * @return a {@link String} object.
     */
    String getProjectName();

    /**
     * getProjectVersion.
     *
     * @return a {@link String} object.
     */
    String getProjectVersion();

    /**
     * isGradle.
     *
     * @return a {@link Boolean} object.
     */
    Boolean isGradle();
}
