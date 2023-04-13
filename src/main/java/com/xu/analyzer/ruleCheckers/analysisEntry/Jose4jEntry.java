package com.xu.analyzer.ruleCheckers.analysisEntry;

import com.xu.analyzer.ruleCheckers.BaseRuleChecker;
import com.xu.analyzer.ruleCheckers.Criteria;

import java.util.ArrayList;
import java.util.List;

public abstract class Jose4jEntry extends BaseRuleChecker {
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private static final List<String> CRITERIA_CLASSES = new ArrayList<>();

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

    }
    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    public String getLibName() {
        return "jose4j";
    }

    public List<String> getCriteriaClasses(){return CRITERIA_CLASSES;}
}
