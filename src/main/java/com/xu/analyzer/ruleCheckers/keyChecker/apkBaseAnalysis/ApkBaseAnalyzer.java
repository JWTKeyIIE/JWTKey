package com.xu.analyzer.ruleCheckers.keyChecker.apkBaseAnalysis;

import com.xu.analyzer.BaseAnalyzer;
import com.xu.analyzer.backward.MethodWrapper;
import com.xu.analyzer.ruleCheckers.BaseRuleChecker;
import com.xu.analyzer.ruleCheckers.Criteria;
import com.xu.analyzer.ruleCheckers.keyChecker.ApkKeyFinder;
import com.xu.utils.FieldInitializationInstructionMap;
import com.xu.utils.NamedMethodMap;
import org.apache.logging.log4j.Logger;
import soot.Scene;
import soot.SootClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApkBaseAnalyzer {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(ApkBaseAnalyzer.class);
    static void analyzeSliceInternal(
            String criteriaClass,
            List<String> classNames,
            String endPoint,
            ArrayList<Integer> slicingParameters,
            ApkKeyFinder checker) {
        SootClass criteriaClazz = Scene.v().getSootClass(criteriaClass);

//        if (criteriaClazz.isPhantomClass()
//                || !criteriaClazz.getMethods().toString().contains(endPoint)) {
//            return;
//        }
        NamedMethodMap.build(classNames);
        NamedMethodMap.addCriteriaClasses(checker.getCriteriaClasses());
        NamedMethodMap.buildCallerCalleeRelation(classNames);
        log.info("end buildCallerCalleeRelation");
        FieldInitializationInstructionMap.build(classNames);
        NamedMethodMap.getMethod(endPoint);
        Map<String, MethodWrapper> methodMap = null;
        for(Criteria criteria : checker.getCriteriaList()){
            String allName = "<" + criteria.getClassName() + ": " + criteria.getMethodName() + ">";
            NamedMethodMap.getMethod(allName);
            methodMap.put(allName, NamedMethodMap.getMethod(allName));
        }
        methodMap.size();
    }
}
