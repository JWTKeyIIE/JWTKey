package com.xu.analyzer.ruleCheckers.keyChecker;

import com.xu.analyzer.ruleCheckers.Criteria;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class JavajwtConstantKeyFinder extends ConstantKeyFinder {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(JavajwtConstantKeyFinder.class);
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private static final List<String> CRITERIA_CLASSES = new ArrayList<>();
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

    public String getLibName() {
        return "java-jwt";
    }

    public List<String> getCriteriaClasses(){return CRITERIA_CLASSES;}
}
