package com.xu.analyzer;

import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.backward.MethodWrapper;
import com.xu.analyzer.backward.UnitContainer;
import com.xu.analyzer.ruleCheckers.BaseRuleChecker;
import com.xu.slicer.backward.MethodCallSiteInfo;
import com.xu.slicer.backward.SlicingCriteria;
import com.xu.slicer.backward.method.MethodInfluenceInstructions;
import com.xu.slicer.backward.method.MethodSlicingResult;
import com.xu.slicer.backward.property.PropertyAnalysisResult;
import com.xu.utils.FieldInitializationInstructionMap;
import com.xu.utils.NamedMethodMap;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;
import soot.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseAnalyzer {
//    public static final List<String> CRITERIA_CLASSES = new ArrayList<>();

    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(BaseAnalyzer.class);

//    static {
///*        CRITERIA_CLASSES.add("javax.crypto.Cipher");
//        CRITERIA_CLASSES.add("java.security.MessageDigest");
//        CRITERIA_CLASSES.add("javax.crypto.spec.SecretKeySpec");
//        CRITERIA_CLASSES.add("javax.crypto.spec.PBEKeySpec");
//        CRITERIA_CLASSES.add("java.security.KeyPairGenerator");
//        CRITERIA_CLASSES.add("java.net.URL");
//        CRITERIA_CLASSES.add("okhttp3.Request$Builder");
//        CRITERIA_CLASSES.add("retrofit2.Retrofit$Builder");
//        CRITERIA_CLASSES.add("javax.crypto.spec.PBEParameterSpec");
//        CRITERIA_CLASSES.add("javax.crypto.spec.IvParameterSpec");
//        CRITERIA_CLASSES.add("java.security.KeyStore");
//        CRITERIA_CLASSES.add("java.security.SecureRandom");*/
//        /*CRITERIA_CLASSES.add("com.auth0.jwt.algorithms.Algorithm");*/
///*        CRITERIA_CLASSES.add("org.jose4j.jwx.JsonWebStructure");
//        CRITERIA_CLASSES.add("org.jose4j.jws.JsonWebSignature");
//        CRITERIA_CLASSES.add("org.jose4j.keys.HmacKey");
//        CRITERIA_CLASSES.add("org.jose4j.jwt.MalformedClaimException");
//        CRITERIA_CLASSES.add("com.google.gson.Gson");
//        CRITERIA_CLASSES.add("org.jose4j.jws.AlgorithmIdentifiers");
//        CRITERIA_CLASSES.add("org.jose4j.lang.JoseException");*/
////        CRITERIA_CLASSES.add("api.util.CommonUtil");
///*        CRITERIA_CLASSES.add("io.jsonwebtoken.impl.DefaultJwtParser");
//        CRITERIA_CLASSES.add("io.jsonwebtoken.Jwts");
//        CRITERIA_CLASSES.add("io.jsonwebtoken.JwtParser");*/
//        CRITERIA_CLASSES.add("io.fusionauth.jwt.hmac.HMACVerifier");
//        CRITERIA_CLASSES.add("io.fusionauth.jwt.Signer");
//        CRITERIA_CLASSES.add("io.fusionauth.jwt.Verifier");
//        CRITERIA_CLASSES.add("io.fusionauth.jwt.domain.JWT");
//        CRITERIA_CLASSES.add("io.fusionauth.jwt.hmac.HMACSigner");
//
//
//    }

    /**
     * BaseAnalyzer都是针对一个切片准则进行处理，例如criteria1.setClassName("javax.crypto.spec.SecretKeySpec");
     * criteria1.setMethodName("void <init>(byte[],java.lang.String)");
     *
     * @param criteriaClass     切片准则对应的类
     * @param classNames        被分析的程序中所有的类的列表
     * @param endPoint          切片准则对应的类和方法
     * @param slicingParameters 切片参数（?? 初步理解要被切片参数是切片方法的第几个参数）
     * @param checker           对应的Checker
     */
    static void analyzeSliceInternal(
            String criteriaClass,
            List<String> classNames,
            String endPoint,
            ArrayList<Integer> slicingParameters,
            BaseRuleChecker checker) {

        SootClass criteriaClazz = Scene.v().getSootClass(criteriaClass);

        if (criteriaClazz.isPhantomClass()
                || !criteriaClazz.getMethods().toString().contains(endPoint)) {
            return;
        }

        NamedMethodMap.build(classNames);

        NamedMethodMap.addCriteriaClasses(checker.CRITERIA_CLASSES);
        NamedMethodMap.buildCallerCalleeRelation(classNames);

        FieldInitializationInstructionMap.build(classNames);

        runBackwardSlicingAnalysis(
                NamedMethodMap.getMethod(endPoint), slicingParameters, null, null, checker);
    }

    /**
     * 处理切片准则为父类中的方法
     *
     * @param criteriaClass
     * @param classNames
     * @param endPoint
     * @param slicingParameters
     * @param checker
     * @param criteriaMethod
     */
    public static void analyzeSliceInternal2(
            String criteriaClass,
            List<String> classNames,
            String endPoint, //"<" + criteriaClass + ": " + criteriaMethod + ">";
            ArrayList<Integer> slicingParameters,
            BaseRuleChecker checker,
            String criteriaMethod) {
        log.info("Begin BaseAnalyzer");
        //将切片准则所在的类表示为SootClass类型
        SootClass criteriaClazz = Scene.v().getSootClass(criteriaClass);
/*        if (criteriaClazz.isPhantomClass()
                || !criteriaClazz.getMethods().toString().contains(endPoint)) {
            return;
        }*/
       /* SootClass superClass = criteriaClazz.getSuperclass();
        String superClassMethods = superClass.getMethods().toString();*/
        if (criteriaClazz.isPhantomClass()
                || (!criteriaClazz.getMethods().toString().contains(endPoint))) {
            return;
        }
        NamedMethodMap.build(classNames);

        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        List<SootClass> subclassesOfCriteriaClass = hierarchy.getDirectSubclassesOf(criteriaClazz);
        if (!subclassesOfCriteriaClass.isEmpty() || !(subclassesOfCriteriaClass == null)) {
            for (SootClass subClass : subclassesOfCriteriaClass) {
                NamedMethodMap.addSubCriteriaClass(subClass.getName());
            }
        } else {
            NamedMethodMap.addCriteriaClasses(checker.CRITERIA_CLASSES);
        }
        NamedMethodMap.buildCallerCalleeRelation(classNames);


//        NamedMethodMap.addCriteriaClasses(CRITERIA_CLASSES);
//        NamedMethodMap.buildCallerCalleeRelation(classNames);

        log.info("NamedMethodMap ");
        FieldInitializationInstructionMap.build(classNames);


        for (SootClass subClass : subclassesOfCriteriaClass) {
            String cName = subClass.getName();
            String key = "<" + cName + ": " + criteriaMethod + ">";
            if (NamedMethodMap.getMethod(key) != null) {
                runBackwardSlicingAnalysis2(
                        NamedMethodMap.getMethod(key),
                        slicingParameters,
                        null,
                        null,
                        checker
                );
            }

        }

/*        runBackwardSlicingAnalysis(
                NamedMethodMap.getMethod(endPoint),
                endPoint,
                slicingParameters,
                null,
                null,
                checker
        );*/
    }

    /**
     * 处理切片准则为父类的切片方法
     *
     * @param criteria
     * @param slicingParams
     * @param methodVsAnalysisResult
     * @param slicingCriteriaMap
     * @param checker
     */
    private static void runBackwardSlicingAnalysis2(
            MethodWrapper criteria,
            List<Integer> slicingParams,
            Map<MethodWrapper, List<Analysis>> methodVsAnalysisResult,
            Map<SlicingCriteria, Boolean> slicingCriteriaMap,
            BaseRuleChecker checker) {

        List<MethodWrapper> callers = criteria.getCallerList();
        if (callers.isEmpty() || slicingParams == null || slicingParams.isEmpty()) {
            return;
        }

        List<MethodCallSiteInfo> callSites = new ArrayList<>();

        //检查调用切片标准方法的方法与切片标准方法之间的调用关系
        for (MethodWrapper caller : callers) {
            for (MethodCallSiteInfo site : caller.getCalleeList()) {
                if (site.getCallee().toString().equals(criteria.toString())) {
                    callSites.add(site);
                }
            }
        }
        for (MethodCallSiteInfo callSiteInfo : callSites) {
            SlicingCriteria slicingCriteria = new SlicingCriteria(callSiteInfo, slicingParams);

            if (methodVsAnalysisResult == null) {
                Map<MethodWrapper, List<Analysis>> newResult = new HashMap<>();
                runBackwardSlicingAnalysisInternal(slicingCriteria, newResult, slicingCriteriaMap, checker);

                for (MethodWrapper methodWrapper : newResult.keySet()) {
                    List<Analysis> analysisList = newResult.get(methodWrapper);
                    for (Analysis analysis : analysisList) {
                        //如果分析结果不是空
                        if (!analysis.getAnalysisResult().isEmpty()) {
                            Utils.NUM_SLICES++;
                            Utils.SLICE_LENGTH.add(analysis.getAnalysisResult().size());
                        }
                        checker.analyzeSlice(analysis);
                    }
                }
                System.gc();
            } else {
                runBackwardSlicingAnalysisInternal(
                        slicingCriteria, methodVsAnalysisResult, slicingCriteriaMap, checker);
            }
        }
    }


    /**
     * @param criteria               "<" + criteriaClass + ": " + criteriaMethod + ">";切片标准方法的MethodWrapper
     * @param slicingParams
     * @param methodVsAnalysisResult
     * @param slicingCriteriaMap
     * @param checker
     */
    private static void runBackwardSlicingAnalysis(
            MethodWrapper criteria,
            List<Integer> slicingParams,
            Map<MethodWrapper, List<Analysis>> methodVsAnalysisResult,
            Map<SlicingCriteria, Boolean> slicingCriteriaMap,
            BaseRuleChecker checker) {

        List<MethodWrapper> callers = criteria.getCallerList();
        if (callers.isEmpty() || slicingParams == null || slicingParams.isEmpty()) {
            return;
        }

        List<MethodCallSiteInfo> callSites = new ArrayList<>();

        //检查调用切片标准方法的方法与切片标准方法之间的调用关系
        for (MethodWrapper caller : callers) {
            for (MethodCallSiteInfo site : caller.getCalleeList()) {
                if (site.getCallee().toString().equals(criteria.toString())) {
                    callSites.add(site);
                }
            }
        }
        for (MethodCallSiteInfo callSiteInfo : callSites) {
            SlicingCriteria slicingCriteria = new SlicingCriteria(callSiteInfo, slicingParams);

            if (methodVsAnalysisResult == null) {
                Map<MethodWrapper, List<Analysis>> newResult = new HashMap<>();
                runBackwardSlicingAnalysisInternal(slicingCriteria, newResult, slicingCriteriaMap, checker);

                for (MethodWrapper methodWrapper : newResult.keySet()) {
                    List<Analysis> analysisList = newResult.get(methodWrapper);
                    for (Analysis analysis : analysisList) {
                        //如果分析结果不是空
                        if (!analysis.getAnalysisResult().isEmpty()) {
                            Utils.NUM_SLICES++;
                            Utils.SLICE_LENGTH.add(analysis.getAnalysisResult().size());
                        }
                        checker.analyzeSlice(analysis);
                    }
                }
                System.gc();
            } else {
                runBackwardSlicingAnalysisInternal(
                        slicingCriteria, methodVsAnalysisResult, slicingCriteriaMap, checker);
            }
        }
    }


    private static void runBackwardSlicingAnalysisInternal(
            SlicingCriteria slicingCriteria,
            Map<MethodWrapper, List<Analysis>> methodVsAnalysisResult,
            Map<SlicingCriteria, Boolean> slicingCriteriaMap,
            BaseRuleChecker checker) {

        if (slicingCriteriaMap == null) {
            slicingCriteriaMap = new HashMap<>();
        }

        MethodCallSiteInfo callSiteInfo = slicingCriteria.getMethodCallSiteInfo();
        List<Integer> slicingParams = slicingCriteria.getParameters();

        //如果切片准则映射关系列表不为空则直接返回
        if (slicingCriteriaMap.get(slicingCriteria) != null) {
            return;
        }
        //将正在处理的切片准则信息放入slicingCriteriaMap中
        slicingCriteriaMap.put(slicingCriteria, Boolean.TRUE);

        MethodSlicingResult methodSlicingResult =
                getInfluencingInstructions(
                        callSiteInfo, slicingParams, callSiteInfo.getCaller().getMethod());

        if (methodSlicingResult.getPropertyUseMap() != null) {
            for (String property : methodSlicingResult.getPropertyUseMap().keySet()) {
                List<PropertyAnalysisResult> propertyAnalysisResults =
                        methodSlicingResult.getPropertyUseMap().get(property);
                for (PropertyAnalysisResult propertyAnalysisResult : propertyAnalysisResults) {

                    List<Analysis> calleeAnalysisList = methodVsAnalysisResult.get(callSiteInfo.getCallee());
                    List<Analysis> callerAnalysisList =
                            methodVsAnalysisResult.get(propertyAnalysisResult.getMethodWrapper());

                    List<Analysis> newAnalysisList =
                            buildNewPropertyAnalysisList(
                                    callSiteInfo,
                                    methodSlicingResult.getAnalysisResult(),
                                    propertyAnalysisResult,
                                    calleeAnalysisList);

                    if (callerAnalysisList == null) {
                        callerAnalysisList = new ArrayList<>();
                        methodVsAnalysisResult.put(
                                propertyAnalysisResult.getMethodWrapper(), callerAnalysisList);
                    }

                    callerAnalysisList.addAll(newAnalysisList);

                    runBackwardSlicingAnalysis(
                            propertyAnalysisResult.getMethodWrapper(),
                            propertyAnalysisResult.getInfluencingParams(),
                            methodVsAnalysisResult,
                            slicingCriteriaMap,
                            checker);
                }
            }
        }
        List<Analysis> calleeAnalysisList = methodVsAnalysisResult.get(callSiteInfo.getCallee());
        List<Analysis> callerAnalysisList = methodVsAnalysisResult.get(callSiteInfo.getCaller());

        List<Analysis> newAnalysisList =
                buildNewAnalysisList(callSiteInfo, methodSlicingResult.getAnalysisResult(), calleeAnalysisList);

        if (callerAnalysisList == null) {
            callerAnalysisList = new ArrayList<>();
            methodVsAnalysisResult.put(callSiteInfo.getCaller(), callerAnalysisList);
        }

        callerAnalysisList.addAll(newAnalysisList);

        runBackwardSlicingAnalysis(
                callSiteInfo.getCaller(),
                methodSlicingResult.getInfluencingParameters(),
                methodVsAnalysisResult,
                slicingCriteriaMap,
                checker);
    }

    private static List<Analysis> buildNewPropertyAnalysisList(
            MethodCallSiteInfo callSiteInfo,
            List<UnitContainer> methodSlicingResult,
            PropertyAnalysisResult slicingResult,
            List<Analysis> calleeAnalysisList) {
        List<Analysis> newAnalysisList = new ArrayList<>();

        //callee 本方法调用的方法分析列表
        if (calleeAnalysisList != null && !calleeAnalysisList.isEmpty()) {
            for (Analysis analysis : calleeAnalysisList) {
                Analysis newAnalysis = new Analysis();

                String newChain =
                        callSiteInfo.getCaller()
                                + "["
                                + callSiteInfo.getLineNumber()
                                + "]"
                                + "--->"
                                + callSiteInfo.getCallee()
                                + "--->"
                                + analysis.getMethodChain();
                newAnalysis.setMethodChain(newChain);
                newAnalysis.setAnalysisResult(analysis.getAnalysisResult());
                newAnalysis.getAnalysisResult().addAll(methodSlicingResult);
                newAnalysis.getAnalysisResult().addAll(slicingResult.getSlicingResult());

                for (String key : slicingResult.getPropertyUseMap().keySet()) {
                    for (PropertyAnalysisResult res : slicingResult.getPropertyUseMap().get(key))
                        newAnalysis.getAnalysisResult().addAll(res.getSlicingResult());
                }

                newAnalysisList.add(newAnalysis);
            }
        } else {
            Analysis newAnalysis = new Analysis();

            String newChain =
                    callSiteInfo.getCaller()
                            + "["
                            + callSiteInfo.getLineNumber()
                            + "]"
                            + "--->"
                            + callSiteInfo.getCallee();
            newAnalysis.setMethodChain(newChain);
            newAnalysis.setAnalysisResult(new ArrayList<>());
            newAnalysis.getAnalysisResult().addAll(methodSlicingResult);
            newAnalysis.getAnalysisResult().addAll(slicingResult.getSlicingResult());

            for (String key : slicingResult.getPropertyUseMap().keySet()) {
                for (PropertyAnalysisResult res : slicingResult.getPropertyUseMap().get(key))
                    newAnalysis.getAnalysisResult().addAll(res.getSlicingResult());
            }

            newAnalysisList.add(newAnalysis);
        }
        return newAnalysisList;
    }

    private static List<Analysis> buildNewAnalysisList(
            MethodCallSiteInfo callSiteInfo,
            List<UnitContainer> slicingResult,
            List<Analysis> calleeAnalysisList) {
        List<Analysis> newAnalysisList = new ArrayList<>();

        if (calleeAnalysisList != null && !calleeAnalysisList.isEmpty()) {
            for (Analysis analysis : calleeAnalysisList) {
                Analysis newAnalysis = new Analysis();

                String newChain =
                        callSiteInfo.getCaller()
                                + "["
                                + callSiteInfo.getLineNumber()
                                + "]"
                                + "--->"
                                + analysis.getMethodChain();
                newAnalysis.setMethodChain(newChain);

                newAnalysis.setAnalysisResult(analysis.getAnalysisResult());
                newAnalysis.getAnalysisResult().addAll(slicingResult);
                newAnalysisList.add(newAnalysis);
            }
        } else {
            Analysis newAnalysis = new Analysis();

            String newChain =
                    callSiteInfo.getCaller()
                            + "["
                            + callSiteInfo.getLineNumber()
                            + "]"
                            + "--->"
                            + callSiteInfo.getCallee();
            newAnalysis.setMethodChain(newChain);
            newAnalysis.setAnalysisResult(new ArrayList<>());
            newAnalysis.getAnalysisResult().addAll(slicingResult);
            newAnalysisList.add(newAnalysis);
        }

        return newAnalysisList;
    }


    private static MethodSlicingResult getInfluencingInstructions(
            MethodCallSiteInfo methodCallSiteInfo, List<Integer> slicingParams, SootMethod m) {
        Body b = m.retrieveActiveBody();

        //ExceptionalUnitGraph 给给定的Body实例构造图，
        // Unit表示方法Body中的每个语句（statement)
        UnitGraph graph = new ExceptionalUnitGraph(b);
        MethodInfluenceInstructions vbe =
                new MethodInfluenceInstructions(graph, methodCallSiteInfo, slicingParams);

        return vbe.getMethodSlicingResult();
    }
}
