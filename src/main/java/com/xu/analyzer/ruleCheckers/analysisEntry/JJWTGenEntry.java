package com.xu.analyzer.ruleCheckers.analysisEntry;

import com.xu.analyzer.ruleCheckers.BaseRuleChecker;
import com.xu.analyzer.ruleCheckers.Criteria;

import java.util.ArrayList;
import java.util.List;

public abstract class JJWTGenEntry extends BaseRuleChecker {
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
        criteria1.setMethodName("io.jsonwebtoken.JwtBuilder signWith(io.jsonwebtoken.SignatureAlgorithm,byte[])");
        criteria1.setParam(1);
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("io.jsonwebtoken.JwtBuilder");
        criteria2.setMethodName("io.jsonwebtoken.JwtBuilder signWith(io.jsonwebtoken.SignatureAlgorithm,java.lang.String)");
        criteria2.setParam(1);
        CRITERIA_LIST.add(criteria2);
        Criteria criteria3 = new Criteria();
        criteria3.setClassName("io.jsonwebtoken.JwtBuilder");
        criteria3.setMethodName("io.jsonwebtoken.JwtBuilder signWith(io.jsonwebtoken.SignatureAlgorithm,java.security.Key)");
        criteria3.setParam(1);
        CRITERIA_LIST.add(criteria3);
        Criteria criteria4 = new Criteria();
        criteria4.setClassName("io.jsonwebtoken.JwtBuilder");
        criteria4.setMethodName("io.jsonwebtoken.JwtBuilder signWith(java.security.Key,io.jsonwebtoken.SignatureAlgorithm)");
        criteria4.setParam(0);
        CRITERIA_LIST.add(criteria4);

        CRITERIA_CLASSES.add("io.jsonwebtoken.JwtBuilder");
    }
    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }
    public String getLibName() {
        return "jjwt";
    }
    public List<String> getCriteriaClasses(){return CRITERIA_CLASSES;}
}
