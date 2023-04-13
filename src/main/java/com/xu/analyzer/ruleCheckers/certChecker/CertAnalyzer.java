package com.xu.analyzer.ruleCheckers.certChecker;

import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.backward.MethodWrapper;
import com.xu.analyzer.backward.UnitContainer;
import com.xu.slicer.backward.MethodCallSiteInfo;
import com.xu.slicer.backward.SlicingCriteria;
import com.xu.slicer.backward.method.MethodInfluenceInstructions;
import com.xu.slicer.backward.method.MethodSlicingResult;
import com.xu.slicer.backward.property.PropertyAnalysisResult;
import com.xu.utils.FieldInitializationInstructionMap;
import com.xu.utils.NamedMethodMap;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertAnalyzer {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(CertAnalyzer.class);

    static void analyzeiceInternal(
            String criteriaClass,
            List<String> classNames,
            String endPoint,
            ArrayList<Integer> slicingParameters,
            CertificateAllFinder checker){

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
    private static void runBackwardSlicingAnalysis(
            MethodWrapper criteria,
            List<Integer> slicingParams,
            Map<MethodWrapper, List<Analysis>> methodVsAnalysisResult,
            Map<SlicingCriteria, Boolean> slicingCriteriaMap,
            CertificateAllFinder checker) {

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
        for(MethodCallSiteInfo callSiteInfo : callSites) {
            SlicingCriteria slicingCriteria = new SlicingCriteria(callSiteInfo,slicingParams);

            if(methodVsAnalysisResult == null) {
                Map<MethodWrapper, List<Analysis>> newResult = new HashMap<>();
                runBackwardSlicingAnalysisInternal(slicingCriteria,newResult,slicingCriteriaMap,checker);

                for (MethodWrapper methodWrapper : newResult.keySet()) {
                    List<Analysis> analysisList = newResult.get(methodWrapper);
                    for(Analysis analysis : analysisList) {
                        //如果分析结果不是空
                        if(!analysis.getAnalysisResult().isEmpty()) {
                            Utils.NUM_SLICES++;
                            Utils.SLICE_LENGTH.add(analysis.getAnalysisResult().size());
                        }
                        checker.analyzeSlice(analysis,methodWrapper);
                    }
                    checker.forwardSlicerAnalysis(methodWrapper);
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
            Map<MethodWrapper,List<Analysis>> methodVsAnalysisResult,
            Map<SlicingCriteria, Boolean> slicingCriteriaMap,
            CertificateAllFinder checker){

        if(slicingCriteriaMap == null) {
            slicingCriteriaMap = new HashMap<>();
        }

        MethodCallSiteInfo callSiteInfo = slicingCriteria.getMethodCallSiteInfo();
        List<Integer> slicingParams = slicingCriteria.getParameters();

        //如果切片准则映射关系列表不为空则直接返回
        if(slicingCriteriaMap.get(slicingCriteria) != null) {
            return;
        }
        //将正在处理的切片准则信息放入slicingCriteriaMap中
        slicingCriteriaMap.put(slicingCriteria, Boolean.TRUE);

        MethodSlicingResult methodSlicingResult =
                getInfluencingInstructions(
                        callSiteInfo, slicingParams, callSiteInfo.getCaller().getMethod());

        if (methodSlicingResult.getPropertyUseMap() != null) {
            for(String property : methodSlicingResult.getPropertyUseMap().keySet()) {
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

        if(callerAnalysisList == null) {
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
            List<Analysis> calleeAnalysisList){
        List<Analysis> newAnalysisList = new ArrayList<>();

        //callee 本方法调用的方法分析列表
        if(calleeAnalysisList != null && !calleeAnalysisList.isEmpty()) {
            for(Analysis analysis : calleeAnalysisList) {
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
        }else {
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
            MethodCallSiteInfo methodCallSiteInfo, List<Integer> slicingParams, SootMethod m){
        Body b = m.retrieveActiveBody();

        //ExceptionalUnitGraph 给给定的Body实例构造图，
        // Unit表示方法Body中的每个语句（statement)
        UnitGraph graph = new ExceptionalUnitGraph(b);
        MethodInfluenceInstructions vbe =
                new MethodInfluenceInstructions(graph,methodCallSiteInfo,slicingParams);

        return vbe.getMethodSlicingResult();
    }

}
