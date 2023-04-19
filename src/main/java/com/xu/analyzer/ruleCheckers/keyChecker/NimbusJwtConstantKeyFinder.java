package com.xu.analyzer.ruleCheckers.keyChecker;

import com.xu.analyzer.ruleCheckers.Criteria;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class NimbusJwtConstantKeyFinder extends ConstantKeyFinder {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(NimbusJwtConstantKeyFinder.class);
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();

    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("com.nimbusds.jose.crypto.MACVerifier");
        criteria0.setMethodName("void <init>(byte[])");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("com.nimbusds.jose.crypto.MACVerifier");
        criteria1.setMethodName("void <init>(java.lang.String)");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("com.nimbusds.jose.crypto.MACVerifier");
        criteria2.setMethodName("void <init>(javax.crypto.SecretKey)");
        criteria2.setParam(0);
        CRITERIA_LIST.add(criteria2);
        Criteria criteria3 = new Criteria();
        criteria3.setClassName("com.nimbusds.jose.crypto.MACVerifier");
        criteria3.setMethodName("void <init>(com.nimbusds.jose.jwk.OctetSequenceKey)");
        criteria3.setParam(0);
        CRITERIA_LIST.add(criteria3);
        Criteria criteria4 = new Criteria();
        criteria4.setClassName("com.nimbusds.jose.crypto.MACVerifier");
        criteria4.setMethodName("void <init>(com.nimbusds.jose.jwk.OctetSequenceKey,java.util.Set)");
        criteria4.setParam(0);
        CRITERIA_LIST.add(criteria4);
        Criteria criteria5 = new Criteria();
        criteria5.setClassName("com.nimbusds.jose.crypto.MACVerifier");
        criteria5.setMethodName("void <init>(byte[],java.util.Set)");
        criteria5.setParam(0);
        CRITERIA_LIST.add(criteria5);

//        CRITERIA_CLASSES.add("com.nimbusds.jose.crypto.MACVerifier");
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public String getLibName() {
        return "nimbusJwt";
    }
}
