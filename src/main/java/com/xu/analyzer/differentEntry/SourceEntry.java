package com.xu.analyzer.differentEntry;

import com.xu.analyzer.ruleCheckers.RuleChecker;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.environmentInit.projectParser.BuildFileParser;
import com.xu.environmentInit.projectParser.BuildFileParserFactory;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SourceEntry implements EntryHandler {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(SourceEntry.class);

    @Override
    public void Scan(EnvironmentInfo info) throws ExceptionHandler {

        log.debug("Retrieving the specific project-based build parser.");

        BuildFileParser buildFileParser = BuildFileParserFactory.getBuildFileParser(info.getSource().get(0));

        info.setTargetProjectName(buildFileParser.getProjectName());
        info.setTargetProjectVersion(buildFileParser.getProjectVersion());
        info.setGradle(buildFileParser.isGradle());

        Map<String, List<String>> moduleVsDependency = buildFileParser.getDependencyList();
        List<String> analyzedModules = new ArrayList<>();

        log.debug("Module Iteration Start");
        for (String module : moduleVsDependency.keySet()) {
            if (!analyzedModules.contains(module)) {
                List<String> dependencies = moduleVsDependency.get(module);
                List<String> otherDependencies = new ArrayList<>();

                log.debug("Dependency Builder Start");
                for (String dependency : dependencies) {
                    log.debug("Dependency name : " + dependency);
                    String dependencyModule;

                    if (dependency.equals(info.getSource().get(0) + "/src/main/java"))
                        dependencyModule = info.getSource().get(0).substring(info.getSource().get(0).lastIndexOf("/") + 1);

                    else
                        dependencyModule =
                                dependency.substring(info.getSource().get(0).length() + 1, dependency.length() - 14);
                    otherDependencies.addAll(info.getDependencies());

                    log.debug("Added the module: " + dependencyModule);
                    analyzedModules.add(dependencyModule);
                }
                log.debug("Dependency Builder Stop");

                log.debug("Starting scanner looper");
                List<RuleChecker> ruleCheckerList;

                switch (info.getJwtLibName()) {
                    case "javajwt":
                        ruleCheckerList = CommenRules.javaJwtRuleList;
                        break;
                    case "jose4j":
                        ruleCheckerList = CommenRules.jose4jRuleList;
                        break;
                    case "nimbus":
                        ruleCheckerList = CommenRules.nimbusRuleList;
                        break;
                    case "jjwt":
                        ruleCheckerList = CommenRules.jjwtRuleList;
                        break;
                    case "fusion":
                        ruleCheckerList = CommenRules.fusionJwtRuleList;
                        break;
                    case "vertx":
                        ruleCheckerList = CommenRules.vertxJwtRuleList;
                        break;
                    default:
                        ruleCheckerList = CommenRules.commonRuleList;
                }

                for (RuleChecker ruleChecker : ruleCheckerList) {
                    log.info("Checking the rule: " + ruleChecker.getClass().getSimpleName());

                    String jwtInfoPath = info.getBuildRootDir().split(":")[0] + "/JWTResult.json";
                    try {
                        ruleChecker.checkRule(
                                SourceType.DIR,
                                dependencies,
                                otherDependencies,
                                info.getSourcePaths(),
                                info.getOutput(),
                                info.getMain(),
                                null,
                                info.getJavaHome(),
                                jwtInfoPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("Scanner looper stopped");

            }
        }

    }
}
