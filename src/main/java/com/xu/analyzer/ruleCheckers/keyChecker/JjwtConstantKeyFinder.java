package com.xu.analyzer.ruleCheckers.keyChecker;

import com.xu.analyzer.ruleCheckers.Criteria;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class JjwtConstantKeyFinder extends ConstantKeyFinder {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(JjwtConstantKeyFinder.class);
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private static final List<String> CRITERIA_CLASSES = new ArrayList<>();
    static {
/*        Criteria criteria0 = new Criteria();
        criteria0.setClassName("io.jsonwebtoken.impl.DefaultJwtParser");
        criteria0.setMethodName("io.jsonwebtoken.JwtParser setSigningKey(byte[])");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("io.jsonwebtoken.impl.DefaultJwtParser");
        criteria1.setMethodName("io.jsonwebtoken.JwtParser setSigningKey(java.lang.String)");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("io.jsonwebtoken.impl.DefaultJwtParser");
        criteria2.setMethodName("io.jsonwebtoken.JwtParser setSigningKey(java.security.Key)");
        criteria2.setParam(0);
        CRITERIA_LIST.add(criteria2);*/
        Criteria criteria3 = new Criteria();
        criteria3.setClassName("io.jsonwebtoken.JwtParser");
        criteria3.setMethodName("io.jsonwebtoken.JwtParser setSigningKey(java.lang.String)");
        criteria3.setParam(0);
        CRITERIA_LIST.add(criteria3);
        Criteria criteria4 = new Criteria();
        criteria4.setClassName("io.jsonwebtoken.JwtParser");
        criteria4.setMethodName("io.jsonwebtoken.JwtParser setSigningKey(byte[])");
        criteria4.setParam(0);
        CRITERIA_LIST.add(criteria4);
        Criteria criteria5 = new Criteria();
        criteria5.setClassName("io.jsonwebtoken.JwtParser");
        criteria5.setMethodName("io.jsonwebtoken.JwtParser setSigningKey(java.security.Key)");
        criteria5.setParam(0);
        CRITERIA_LIST.add(criteria5);

        CRITERIA_CLASSES.add("io.jsonwebtoken.impl.DefaultJwtParser");
        CRITERIA_CLASSES.add("io.jsonwebtoken.Jwts");
        CRITERIA_CLASSES.add("io.jsonwebtoken.JwtParser");
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public String getLibName() {
        return "jjwt";
    }
    public List<String> getCriteriaClasses(){return CRITERIA_CLASSES;}
}
