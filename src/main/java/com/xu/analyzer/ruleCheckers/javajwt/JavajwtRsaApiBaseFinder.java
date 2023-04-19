package com.xu.analyzer.ruleCheckers.javajwt;

import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.ruleCheckers.BaseRuleChecker;
import com.xu.analyzer.ruleCheckers.Criteria;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavajwtRsaApiBaseFinder extends BaseRuleChecker {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(JavajwtRsaApiBaseFinder.class);
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    public static final List<Analysis> analysis_result = new ArrayList<>();
    static {
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("com.auth0.jwt.JWT");
        criteria1.setMethodName("com.auth0.jwt.interfaces.Verification require(com.auth0.jwt.algorithms.Algorithm)");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);

/*        CRITERIA_CLASSES.add("com.auth0.jwt.JWT");
        CRITERIA_CLASSES.add("com.auth0.jwt.JWTVerifier");
        CRITERIA_CLASSES.add("com.auth0.jwt.algorithms.Algorithm");
        CRITERIA_CLASSES.add("com.auth0.jwt.JWTCreator");
        CRITERIA_CLASSES.add("com.auth0.jwt.interfaces.DecodedJWT");

        CRITERIA_CLASSES.add("com.auth0.jwt.exceptions.JWTVerificationException");
        CRITERIA_CLASSES.add("java.io.IOException");
        CRITERIA_CLASSES.add("java.security.KeyFactory");
        CRITERIA_CLASSES.add("java.security.spec.X509EncodedKeySpec");

        CRITERIA_CLASSES.add("java.io.IOException");
        CRITERIA_CLASSES.add("java.security.PrivateKey");
        CRITERIA_CLASSES.add("java.security.PublicKey");
        CRITERIA_CLASSES.add("java.security.interfaces.RSAPrivateKey");
        CRITERIA_CLASSES.add("java.security.interfaces.RSAPublicKey");*/
    }


    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public void analyzeSlice(Analysis analysis) {
        analysis_result.add(analysis);
    }

    @Override
    public void createAnalysisOutput(Map<String, String> xmlFileStr, List<String> sourcePaths, OutputStructure output) throws ExceptionHandler {
        log.info("Java jwt api Finder FirstStep");
    }

    public List<Analysis> getAnalysis_result() {
        return analysis_result;
    }

}
