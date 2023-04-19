package com.xu.analyzer.ruleCheckers.keyChecker;

import com.xu.analyzer.ruleCheckers.Criteria;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Jose4jConstantKeyFinder extends ConstantKeyFinder {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(Jose4jConstantKeyFinder.class);
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();

    static {
/*        Criteria criteria0 = new Criteria();
        criteria0.setClassName("org.jose4j.jwx.JsonWebStructure");
        criteria0.setMethodName("void setKey(java.security.Key)");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);
        Criteria criteria3 = new Criteria();
        criteria3.setClassName("org.jose4j.jws.JsonWebSignature");
        criteria3.setMethodName("void setKey(java.security.Key)");
        CRITERIA_LIST.add(criteria3);*/
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("org.jose4j.keys.HmacKey");
        criteria1.setMethodName("void <init>(byte[])");
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("org.jose4j.keys.AesKey");
        criteria2.setMethodName("void <init>(byte[])");
        CRITERIA_LIST.add(criteria2);

/*        CRITERIA_CLASSES.add("org.jose4j.jwx.JsonWebStructure");
        CRITERIA_CLASSES.add("org.jose4j.jws.JsonWebSignature");
        CRITERIA_CLASSES.add("org.jose4j.keys.HmacKey");
        CRITERIA_CLASSES.add("org.jose4j.jwt.MalformedClaimException");
        CRITERIA_CLASSES.add("org.jose4j.jws.AlgorithmIdentifiers");
        CRITERIA_CLASSES.add("org.jose4j.lang.JoseException");*/
    }
    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public String getLibName() {
        return "jose4j";
    }
}
