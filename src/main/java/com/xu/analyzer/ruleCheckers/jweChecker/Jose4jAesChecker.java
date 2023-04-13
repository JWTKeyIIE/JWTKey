package com.xu.analyzer.ruleCheckers.jweChecker;

import com.xu.analyzer.ruleCheckers.Criteria;
import com.xu.analyzer.ruleCheckers.keyChecker.ConstantKeyFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Jose4jAesChecker extends ConstantKeyFinder {
    private static final Logger log = (Logger) org.apache.logging.log4j.LogManager.getLogger(Jose4jAesChecker.class);
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private static final List<String> CRITERIA_CLASSES = new ArrayList<>();
    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("org.jose4j.keys.AesKey");
        criteria0.setMethodName("void <init>(byte[])");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);

        CRITERIA_CLASSES.add("org.jose4j.keys.AesKey");
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public String getLibName() {
        return "jose4j";
    }

    @Override
    public List<String> getCriteriaClasses() {
        return CRITERIA_CLASSES;
    }
}
