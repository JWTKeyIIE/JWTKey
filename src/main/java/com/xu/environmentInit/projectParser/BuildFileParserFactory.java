package com.xu.environmentInit.projectParser;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class BuildFileParserFactory {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(BuildFileParserFactory.class);

    public static BuildFileParser getBuildFileParser(String projectRoot) throws ExceptionHandler {
        File pomFile = new File(projectRoot + "/" + "pom.xml");
        if(pomFile.exists()){
            return new MvnPomFileParser(projectRoot + "/" + "pom.xml");
        }

        File gradleFile = new File(projectRoot + "/" + "settings.gradle");

        if(gradleFile.exists()){
            return new GradleBuildFileParser(projectRoot + "/" + "settings.gradle");
        }

        log.fatal("Only Maven and Gradle Projects are supported");
        throw new ExceptionHandler(
                "Only Maven and Gradle Projects are supported", ExceptionId.ARG_VALID);

    }
}
