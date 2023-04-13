package com.xu.analyzer.ruleCheckers;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;

import java.io.IOException;
import java.util.List;

public interface RuleChecker {
    void checkRule(
            SourceType type,
            List<String> projectJarPath,
            List<String> projectDependencyPath,
            List<String> sourcePaths,
            OutputStructure output,
            String mainKlass,
            String androidHome,
            String javaHome,
            String jwtInfoPath)
            throws ExceptionHandler, IOException;
}
