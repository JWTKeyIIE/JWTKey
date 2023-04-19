package com.xu.analyzer.ruleCheckers.keyChecker;

import com.xu.analyzer.ruleCheckers.Criteria;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class FusionauthConstantKeyFinder extends ConstantKeyFinder {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(FusionauthConstantKeyFinder.class);
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("io.fusionauth.jwt.hmac.HMACVerifier");
        criteria0.setMethodName("void <init>(byte[],io.fusionauth.security.CryptoProvider)");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("io.fusionauth.jwt.hmac.HMACVerifier");
        criteria1.setMethodName("io.fusionauth.jwt.hmac.HMACVerifier newVerifier(java.lang.String)");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("io.fusionauth.jwt.hmac.HMACVerifier");
        criteria2.setMethodName("io.fusionauth.jwt.hmac.HMACVerifier newVerifier(byte[])");
        criteria2.setParam(0);
        CRITERIA_LIST.add(criteria2);
        Criteria criteria3 = new Criteria();
        criteria3.setClassName("io.fusionauth.jwt.hmac.HMACVerifier");
        criteria3.setMethodName("io.fusionauth.jwt.hmac.HMACVerifier newVerifier(java.lang.String,io.fusionauth.security.CryptoProvider)");
        criteria3.setParam(3);
        CRITERIA_LIST.add(criteria3);
        Criteria criteria4 = new Criteria();
        criteria4.setClassName("io.fusionauth.jwt.hmac.HMACVerifier");
        criteria4.setMethodName("io.fusionauth.jwt.hmac.HMACVerifier newVerifier(byte[],io.fusionauth.security.CryptoProvider)");
        criteria4.setParam(2);
        CRITERIA_LIST.add(criteria4);

/*        CRITERIA_CLASSES.add("io.fusionauth.jwt.hmac.HMACVerifier");
        CRITERIA_CLASSES.add("io.fusionauth.jwt.Signer");
        CRITERIA_CLASSES.add("io.fusionauth.jwt.Verifier");
        CRITERIA_CLASSES.add("io.fusionauth.jwt.domain.JWT");
        CRITERIA_CLASSES.add("io.fusionauth.jwt.hmac.HMACSigner");
        CRITERIA_CLASSES.add("org.springframework.beans.factory.annotation.Value");
        CRITERIA_CLASSES.add("org.springframework.stereotype.Component");*/
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public String getLibName() {
        return "FusionAuth";
    }
}
