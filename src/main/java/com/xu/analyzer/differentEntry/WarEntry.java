package com.xu.analyzer.differentEntry;

import com.xu.analyzer.ruleCheckers.RuleChecker;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.enumClass.SourceType;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class WarEntry implements EntryHandler {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WarEntry.class);

    @Override
    public void Scan(EnvironmentInfo info) throws ExceptionHandler {
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
                        SourceType.WAR,
                        info.getSource(),
                        info.getDependencies(),
                        info.getSourcePaths(),
                        info.getOutput(),
                        info.getMain(),
                        null,
                        info.getJavaHome(),
                        jwtInfoPath);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ExceptionHandler e) {
                e.printStackTrace();
            }
        }
        log.debug("Scanner looper stopped");
    }
}
