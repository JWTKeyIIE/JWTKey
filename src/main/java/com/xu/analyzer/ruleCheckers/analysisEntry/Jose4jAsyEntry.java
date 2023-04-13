package com.xu.analyzer.ruleCheckers.analysisEntry;

import com.xu.analyzer.ruleCheckers.BaseRuleChecker;
import com.xu.analyzer.ruleCheckers.Criteria;

import java.util.ArrayList;
import java.util.List;

public abstract class Jose4jAsyEntry extends BaseRuleChecker {
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private static final List<String> CRITERIA_CLASSES = new ArrayList<>();
    static {
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
