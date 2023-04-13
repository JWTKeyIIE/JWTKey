package com.xu.analyzer.ruleCheckers.analysisEntry;

import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.ruleCheckers.BaseRuleChecker;
import com.xu.analyzer.ruleCheckers.Criteria;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class JavaJwtEntry extends BaseRuleChecker {
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private static final List<String> CRITERIA_CLASSES = new ArrayList<>();
//由于Java JWT不区分生成和验证时指定的密钥，因此入口统一
    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("com.auth0.jwt.algorithms.Algorithm");
        criteria0.setMethodName("com.auth0.jwt.algorithms.Algorithm HMAC256(java.lang.String)");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("com.auth0.jwt.algorithms.Algorithm");
        criteria1.setMethodName("com.auth0.jwt.algorithms.Algorithm HMAC384(java.lang.String)");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("com.auth0.jwt.algorithms.Algorithm");
        criteria2.setMethodName("com.auth0.jwt.algorithms.Algorithm HMAC512(java.lang.String)");
        criteria2.setParam(0);
        CRITERIA_LIST.add(criteria2);
        Criteria criteria3 = new Criteria();
        criteria3.setClassName("com.auth0.jwt.algorithms.HMACAlgorithm");
        criteria3.setMethodName("com.auth0.jwt.algorithms.HMACAlgorithm: void <init>(com.auth0.jwt.algorithms.CryptoHelper,java.lang.String,java.lang.String,byte[])");
        criteria3.setParam(3);
        CRITERIA_LIST.add(criteria3);
        Criteria criteria4 = new Criteria();
        criteria4.setClassName("com.auth0.jwt.algorithms.HMACAlgorithm");
        criteria4.setMethodName("com.auth0.jwt.algorithms.HMACAlgorithm: void <init>(java.lang.String,java.lang.String,byte[])");
        criteria4.setParam(2);
        CRITERIA_LIST.add(criteria4);
        Criteria criteria5 = new Criteria();
        criteria5.setClassName("com.auth0.jwt.algorithms.HMACAlgorithm");
        criteria5.setMethodName("com.auth0.jwt.algorithms.HMACAlgorithm: void <init>(java.lang.String,java.lang.String,java.lang.String)");
        criteria5.setParam(2);
        CRITERIA_LIST.add(criteria5);


        CRITERIA_CLASSES.add("com.auth0.jwt.algorithms.Algorithm");
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public List<String> getCriteriaClasses() {
        return CRITERIA_CLASSES;
    }

    public String getLibName() {
        return "java-jwt";
    }

}
