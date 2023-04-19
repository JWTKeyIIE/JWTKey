package com.xu.analyzer.ruleCheckers.certChecker;

import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.backward.MethodWrapper;
import com.xu.analyzer.backward.UnitContainer;
import com.xu.analyzer.ruleCheckers.Criteria;
import com.xu.analyzer.ruleCheckers.RuleChecker;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.output.AnalysisIssue;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import com.xu.slicer.forward.ForwardInfluenceInstructions;
import com.xu.slicer.forward.SlicingCriteria;
import com.xu.slicer.forward.SlicingResult;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;
import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.IOException;
import java.util.*;


public class CertificateAllFinder implements RuleChecker {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(CertificateAllFinder.class);
    public static final List<String> CRITERIA_CLASSES = new ArrayList<>();
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();

    private Map<String, CertVerifyAnalysisResult> CertVerifyResult = new HashMap<>();
    private CertVerifyAnalysisResult currentCertResult;
    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("java.security.cert.CertificateFactory");
        criteria0.setMethodName("java.security.cert.Certificate generateCertificate(java.io.InputStream)");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);

        CRITERIA_CLASSES.add("java.security.cert.CertificateFactory");
    }

    @Override
    public void checkRule(SourceType type, List<String> projectPaths, List<String> projectDependencyPath, List<String> sourcePaths, OutputStructure output, String mainKlass, String androidHome, String javaHome, String jwtInfoPath) throws ExceptionHandler, IOException {

        String[] excludes = {"web.xml", "pom.xml"};
        Map<String, String> xmlFileStr =
                Utils.getXmlFiles(projectPaths.get(0), Arrays.asList(excludes));
        for(Criteria criteria : getCriteriaList()){
            CertAnalysisRouting.environmentRouting(type,
                    criteria.getClassName(),
                    criteria.getMethodName(),
                    criteria.getParam(),
                    projectPaths,
                    projectDependencyPath,
                    this,
                    mainKlass,
                    androidHome,
                    javaHome,
                    jwtInfoPath);
        }
        for(String key : CertVerifyResult.keySet()){
            CertVerifyAnalysisResult certVerifyAnalysisResult = CertVerifyResult.get(key);
            if(certVerifyAnalysisResult.isCertFromX5C && !certVerifyAnalysisResult.getX5cSootString().contains("jose4j")){
                output.addIssue(
                        new AnalysisIssue(certVerifyAnalysisResult.getX5cSootString(),8,"Find unsecure Cert verify with x5c parameter",sourcePaths,null)
                );
            }
        }

    }
    public void analyzeSlice(Analysis analysis, MethodWrapper currentMethod) {
        if (analysis.getAnalysisResult().isEmpty()){
            log.info("Analysis result is null");
            return;
        }
//        log.info("Begin to analysis result");
        CertVerifyAnalysisResult certRuslt = new CertVerifyAnalysisResult(currentMethod.getMethod().getName());
        boolean isUseX5c = false;
        String x5cString = "";
        this.currentCertResult = certRuslt;
        for(int index = 0; index < analysis.getAnalysisResult().size(); index++){
            UnitContainer e = analysis.getAnalysisResult().get(index);
            if(e.getUnit().toString().contains("x5c")){
                isUseX5c = true;
                x5cString = e.getMethod();
                continue;
            }
        }
        if(!isUseX5c){
            if(currentMethod.getMethod().getActiveBody().toString().contains("x5c"))
            {
                isUseX5c = true;
                x5cString = currentMethod.getMethod().toString();
            }
            isUseX5c = searchX5CInCaller(currentMethod.getCallerList());
            x5cString = currentMethod.getMethod().toString();
        }
        currentCertResult.setCertFromX5C(isUseX5c);
        currentCertResult.setX5cSootString(x5cString);
    }

    public void forwardSlicerAnalysis(MethodWrapper currentMethod){
        //forward slicing to find Certificate verify method
        if(!currentCertResult.getAnalysisMethod().contains(currentMethod.getMethod().getName())){
            return;
        }
        SlicingCriteria slicingCriteria = new SlicingCriteria("<java.security.cert.CertificateFactory: java.security.cert.Certificate generateCertificate(java.io.InputStream)>");
        List<SlicingResult> slicingResults = getInfluencingInstructions(slicingCriteria, currentMethod.getMethod());
        for(SlicingResult slicingResult : slicingResults) {
            slicingResult.getAnalysisResult();
            for (int index = 0; index < slicingResult.getAnalysisResult().size(); index++) {
                Unit u = slicingResult.getAnalysisResult().get(index);
                if (u.toString().contains("java.security.cert.X509Certificate: void verify")) {
                    System.out.println("Verify cert signature");
                    currentCertResult.setVerifySig(true);
                }
                if (u.toString().contains("java.security.cert.X509Certificate: void checkValidity"))
                    currentCertResult.setVerifyDate(true);
                if (u.toString().contains("return")) {
                    searchVerifyInCaller(new SlicingCriteria(currentMethod.toString()), currentMethod.getCallerList());
                }
            }
        }
        searchVerifyChain(currentMethod);
        currentCertResult.setMethodChain("className: " + currentMethod.getMethod().getDeclaringClass()
                + " Method: " + currentMethod.getMethod().getName());
        CertVerifyResult.put(currentCertResult.getAnalysisMethod(),currentCertResult);
    }

    private void searchVerifyChain(MethodWrapper currentMethod){
        if(!currentCertResult.getAnalysisMethod().contains(currentMethod.getMethod().getName())){
            return;
        }
        SlicingCriteria slicingCriteria = new SlicingCriteria("<java.security.cert.CertificateFactory: java.security.cert.CertPath generateCertPath(java.util.List)>");
        List<SlicingResult> slicingResults = getInfluencingInstructions(slicingCriteria, currentMethod.getMethod());
        for(SlicingResult slicingResult : slicingResults){
            for(int index = 0; index < slicingResult.getAnalysisResult().size(); index ++) {
                Unit u = slicingResult.getAnalysisResult().get(index);
                if(u.toString().contains("java.security.cert.CertPathValidator: java.security.cert.CertPathValidatorResult validate")){
                    currentCertResult.setVerifyChain(true);
                    continue;
                }
                if(u.toString().contains("invoke <")){
                    for(int i = 0; i < currentMethod.getCalleeList().size(); i++){
                        if(u.toString().contains(currentMethod.getCalleeList().get(i).getCallee().toString())){
                            SootMethod method = currentMethod.getCalleeList().get(i).getCallee().getMethod();
                            if(method.retrieveActiveBody().toString().contains("java.security.cert.CertPathValidator: java.security.cert.CertPathValidatorResult validate"))
                                currentCertResult.setVerifyChain(true);
                        }
                    }
                }
            }
        }
    }

    private void searchVerifyInCaller(SlicingCriteria slicingCriteria, List<MethodWrapper> callerMethods){
        for(int index = 0; index < callerMethods.size(); index ++){
            List<SlicingResult> callerSlicingResults = getInfluencingInstructions(slicingCriteria, callerMethods.get(index).getMethod());
            for(SlicingResult callerSlicingResult : callerSlicingResults) {
                for (int index_2 = 0; index_2 < callerSlicingResult.getAnalysisResult().size(); index_2++) {
                    Unit u = callerSlicingResult.getAnalysisResult().get(index_2);
                    if (u.toString().contains("java.security.cert.X509Certificate: void verify"))
                        currentCertResult.setVerifySig(true);
                    if (u.toString().contains("java.security.cert.X509Certificate: void checkValidity"))
                        currentCertResult.setVerifyDate(true);
                    if (u.toString().contains("return"))
                        continue;
                    if (index_2 == callerSlicingResult.getAnalysisResult().size() - 1) {
                        if (callerMethods.get(index).getMethod().getActiveBody().toString().contains("java.security.cert.X509Certificate: void verify"))
                            currentCertResult.setVerifySig(true);
                        if (callerMethods.get(index).getMethod().getActiveBody().toString().contains("java.security.cert.X509Certificate: void checkValidity"))
                            currentCertResult.setVerifyDate(true);
                    }
                }
            }
        }
    }


    private boolean searchX5CInCaller(List<MethodWrapper> callerMethod){
        for(MethodWrapper methodWrapper : callerMethod){
            SootMethod m = methodWrapper.getMethod();
            Body b =  m.getActiveBody();
            if(b.toString().contains("x5c")){
                return true;
            }
        }
        return false;
    }


    public static List<SlicingResult> getInfluencingInstructions(
            SlicingCriteria slicingCriteria, SootMethod m) {
        if (m.isConcrete()) {

            Body b = m.retrieveActiveBody();

            UnitGraph graph = new ExceptionalUnitGraph(b);
            ForwardInfluenceInstructions vbe = new ForwardInfluenceInstructions(graph, slicingCriteria);
            return vbe.getSlicingResults();
        }
        return null;
    }
    public static List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

}
