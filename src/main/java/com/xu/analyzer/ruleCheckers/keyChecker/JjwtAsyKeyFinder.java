package com.xu.analyzer.ruleCheckers.keyChecker;

import com.xu.analyzer.ruleCheckers.Criteria;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JjwtAsyKeyFinder extends ApkKeyFinder{
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(JjwtAsyKeyFinder.class);
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private static final List<String> CRITERIA_CLASSES = new ArrayList<>();
    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("io.jsonwebtoken.JwtBuilder");
        criteria0.setMethodName("io.jsonwebtoken.JwtBuilder signWith(java.security.Key)");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("io.jsonwebtoken.JwtBuilder");
        criteria1.setMethodName("io.jsonwebtoken.JwtBuilder signWith(java.security.Key,io.jsonwebtoken.SignatureAlgorithm)");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("io.jsonwebtoken.JwtBuilder");
        criteria2.setMethodName("io.jsonwebtoken.JwtBuilder signWith(io.jsonwebtoken.SignatureAlgorithm,byte[])");
        criteria2.setParam(1);
        CRITERIA_LIST.add(criteria2);
        Criteria criteria3 = new Criteria();
        criteria3.setClassName("io.jsonwebtoken.JwtBuilder");
        criteria3.setMethodName("io.jsonwebtoken.JwtBuilder signWith(io.jsonwebtoken.SignatureAlgorithm,java.lang.String)>");
        criteria3.setParam(1);
        CRITERIA_LIST.add(criteria3);
        Criteria criteria4 = new Criteria();
        criteria4.setClassName("io.jsonwebtoken.JwtBuilder");
        criteria4.setMethodName("signWith(java.security.Key,io.jsonwebtoken.SignatureAlgorithm)>");
        criteria4.setParam(0);
        CRITERIA_LIST.add(criteria4);
        Criteria criteria5 = new Criteria();
        criteria5.setClassName("io.jsonwebtoken.JwtParser");
        criteria5.setMethodName("io.jsonwebtoken.JwtParser setSigningKey(java.security.Key)");
        criteria5.setParam(0);
        CRITERIA_LIST.add(criteria5);
        Criteria criteria6 = new Criteria();
        criteria6.setClassName("io.jsonwebtoken.JwtParser");
        criteria6.setMethodName("io.jsonwebtoken.JwtParser setSigningKey(java.security.Key)");
        criteria6.setParam(0);
        CRITERIA_LIST.add(criteria6);
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public void createAnalysisOutput(Map<String, String> xmlFileStr, List<String> sourcePaths, OutputStructure output) throws ExceptionHandler {

    }

    @Override
    public String getLibName() {
        return "jjwt";
    }

    @Override
    public List<String> getCriteriaClasses() {
        return CRITERIA_CLASSES;
    }
}
