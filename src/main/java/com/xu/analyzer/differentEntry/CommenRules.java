package com.xu.analyzer.differentEntry;

import com.xu.analyzer.ruleCheckers.*;
import com.xu.analyzer.ruleCheckers.certChecker.CertificateAllFinder;
import com.xu.analyzer.ruleCheckers.httpChecker.FusionauthHttpFinder;
import com.xu.analyzer.ruleCheckers.httpChecker.JavaJwtHttpFinder;
import com.xu.analyzer.ruleCheckers.httpChecker.Jose4jHttpFinder;
import com.xu.analyzer.ruleCheckers.httpChecker.NimbusJwtHttpFinder;
import com.xu.analyzer.ruleCheckers.javajwt.JavaJwtRsaHardcodedKeyFinder;
import com.xu.analyzer.ruleCheckers.keyChecker.*;
import com.xu.analyzer.ruleCheckers.javajwt.JavajwtRsaApiFinder;
import com.xu.analyzer.ruleCheckers.springProject.SpringValueChecker;

import java.util.ArrayList;
import java.util.List;

public class CommenRules {
    public static List<RuleChecker> javaJwtRuleList = new ArrayList<>();
    public static List<RuleChecker> jose4jRuleList = new ArrayList<>();
    public static List<RuleChecker> nimbusRuleList = new ArrayList<>();
    public static List<RuleChecker> jjwtRuleList = new ArrayList<>();
    public static List<RuleChecker> fusionJwtRuleList = new ArrayList<>();
    public static List<RuleChecker> vertxJwtRuleList = new ArrayList<>();

    public static List<RuleChecker> commonRuleList = new ArrayList<>();

    static {
        /**
         * java-jwt
         */
//        javaJwtRuleList.add(new CertificateAllFinder());
//        javaJwtRuleList.add(new JavajwtConstantKeyFinder());
//        javaJwtRuleList.add(new JavaJwtHttpFinder());
//        javaJwtRuleList.add(new JavajwtRsaApiFinder());
//        javaJwtRuleList.add(new JavaJwtRsaHardcodedKeyFinder());
//        javaJwtRuleList.add(new SpringValueChecker());
//        javaJwtRuleList.add(new CertificateAllFinder());
        /**
         * Jose4j
         */
        jose4jRuleList.add(new Jose4jConstantKeyFinder());
        jose4jRuleList.add(new Jose4jHttpFinder());
        jose4jRuleList.add(new SpringValueChecker());
//        jose4jRuleList.add(new CertificateAllFinder());
        /**
         * nimbus
         */
        nimbusRuleList.add(new NimbusJwtConstantKeyFinder());
        nimbusRuleList.add(new NimbusJwtHttpFinder());
        nimbusRuleList.add(new SpringValueChecker());
//        nimbusRuleList.add(new CertificateAllFinder());
        /**
         * jjwt
         */
        jjwtRuleList.add(new CertificateAllFinder());
        jjwtRuleList.add(new JjwtConstantKeyFinder());
        jjwtRuleList.add(new SpringValueChecker());
//        jjwtRuleList.add(new JjwtAsyKeyFinder());
        /**
         * fusionJwt
         */
        fusionJwtRuleList.add(new FusionauthConstantKeyFinder());
        fusionJwtRuleList.add(new FusionauthHttpFinder());
        fusionJwtRuleList.add(new SpringValueChecker());
//        fusionJwtRuleList.add(new CertificateAllFinder());
        /**
         * Vertx Jwt
         */
        //Other
        //short Rsa Key
        vertxJwtRuleList.add(new SpringValueChecker());
//        vertxJwtRuleList.add(new CertificateAllFinder());

        /**
         * List contain all rule checker
         */
        commonRuleList.add(new JavajwtConstantKeyFinder());
        commonRuleList.add(new JavaJwtHttpFinder());
        commonRuleList.add(new JavajwtRsaApiFinder());
        commonRuleList.add(new JavaJwtRsaHardcodedKeyFinder());

        commonRuleList.add(new Jose4jConstantKeyFinder());
        commonRuleList.add(new Jose4jHttpFinder());

        commonRuleList.add(new NimbusJwtConstantKeyFinder());
        commonRuleList.add(new NimbusJwtHttpFinder());

        commonRuleList.add(new JjwtConstantKeyFinder());

        commonRuleList.add(new FusionauthConstantKeyFinder());
        commonRuleList.add(new FusionauthHttpFinder());

        //Other
        //short Rsa Key
        commonRuleList.add(new SpringValueChecker());
//        commonRuleList.add(new CertificateAllFinder());
    }
}
