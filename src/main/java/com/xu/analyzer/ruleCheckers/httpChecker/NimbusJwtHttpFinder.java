package com.xu.analyzer.ruleCheckers.httpChecker;

import com.xu.analyzer.ruleCheckers.Criteria;

import java.util.ArrayList;
import java.util.List;

public class NimbusJwtHttpFinder extends UnsecureHttpFinder{
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private static final List<String> CRITERIA_CLASSES = new ArrayList<>();
    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("com.nimbusds.jose.jwk.source.RemoteJWKSet");
        criteria0.setMethodName("void <init>(java.net.URL)");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("com.nimbusds.jose.jwk.source.RemoteJWKSet");
        criteria1.setMethodName("void <init>(java.net.URL,com.nimbusds.jose.util.ResourceRetriever)");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("com.nimbusds.jose.jwk.source.RemoteJWKSet");
        criteria2.setMethodName("void <init>(java.net.URL,com.nimbusds.jose.util.ResourceRetriever,com.nimbusds.jose.jwk.source.JWKSetCache)");
        criteria2.setParam(0);
        CRITERIA_LIST.add(criteria2);

//        CRITERIA_CLASSES.add("com.nimbusds.jose.jwk.source.RemoteJWKSet");
    }
    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public List<String> getCriteriaClasses() {
        return CRITERIA_CLASSES;
    }

    @Override
    public String getJwtLibName() {
        return "nimbusJwt";
    }
}
