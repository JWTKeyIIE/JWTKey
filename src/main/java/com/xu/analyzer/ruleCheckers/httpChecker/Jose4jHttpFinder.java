package com.xu.analyzer.ruleCheckers.httpChecker;

import com.xu.analyzer.ruleCheckers.Criteria;

import java.util.ArrayList;
import java.util.List;

public class Jose4jHttpFinder extends UnsecureHttpFinder{
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("org.jose4j.jwk.HttpsJwks");
        criteria0.setMethodName("void <init>(java.lang.String)");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);

//        CRITERIA_CLASSES.add("org.jose4j.jwk.HttpsJwks");
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public String getJwtLibName() {
        return "jose4j";
    }
}
