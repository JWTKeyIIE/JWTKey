package com.xu.analyzer.ruleCheckers.jweChecker;

import com.xu.analyzer.ruleCheckers.Criteria;
import com.xu.analyzer.ruleCheckers.keyChecker.ConstantKeyFinder;

import java.util.ArrayList;
import java.util.List;

public class NimbusAesChecker extends ConstantKeyFinder {
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private static final List<String> CRITERIA_CLASSES = new ArrayList<>();

    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("com.nimbusds.jose.crypto.DirectEncrypter");
        criteria0.setMethodName("void <init>(javax.crypto.SecretKey)");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("com.nimbusds.jose.crypto.DirectEncrypter");
        criteria1.setMethodName("void <init>(byte[])");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("com.nimbusds.jose.crypto.DirectEncrypter");
        criteria2.setMethodName("void <init>(com.nimbusds.jose.jwk.OctetSequenceKey)");
        criteria2.setParam(0);
        CRITERIA_LIST.add(criteria2);
        Criteria criteria3 = new Criteria();
        criteria3.setClassName("com.nimbusds.jose.crypto.DirectDecrypter");
        criteria3.setMethodName("void <init>(javax.crypto.SecretKey)");
        criteria3.setParam(0);
        CRITERIA_LIST.add(criteria3);
        Criteria criteria4 = new Criteria();
        criteria4.setClassName("com.nimbusds.jose.crypto.DirectDecrypter");
        criteria4.setMethodName("void <init>(javax.crypto.SecretKey,boolean)");
        criteria4.setParam(0);
        CRITERIA_LIST.add(criteria4);
        Criteria criteria5 = new Criteria();
        criteria5.setClassName("com.nimbusds.jose.crypto.DirectDecrypter");
        criteria5.setMethodName("void <init>(byte[])");
        criteria5.setParam(0);
        CRITERIA_LIST.add(criteria5);
        Criteria criteria6 = new Criteria();
        criteria6.setClassName("com.nimbusds.jose.crypto.DirectDecrypter");
        criteria6.setMethodName("void <init>(com.nimbusds.jose.jwk.OctetSequenceKey)");
        criteria6.setParam(0);
        CRITERIA_LIST.add(criteria6);

        CRITERIA_CLASSES.add("com.nimbusds.jose.crypto.DirectEncrypter");
        CRITERIA_CLASSES.add("com.nimbusds.jose.crypto.DirectEncrypter");

    }

    @Override
    public List<Criteria> getCriteriaList() {
        return null;
    }

    @Override
    public String getLibName() {
        return null;
    }

    @Override
    public List<String> getCriteriaClasses() {
        return null;
    }
}
