package com.xu.analyzer.ruleCheckers.javajwt;

import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.backward.AssignInvokeUnitContainer;
import com.xu.analyzer.backward.UnitContainer;
import com.xu.analyzer.ruleCheckers.RuleChecker;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import org.apache.logging.log4j.Logger;
import soot.jimple.internal.JAssignStmt;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavajwtRsaApiFinder implements RuleChecker {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(JavajwtRsaApiFinder.class);
    private List<Analysis> first_result;
    private List<Analysis> second_result;
    private static final List<String> ANALYSIS_LIST = new ArrayList<>();
    public List<JavaJwtRsaApiResult> analysis_result = new ArrayList<>();
    private static final Pattern methodChain = Pattern.compile("[<](.+)[:]");

    static {
        ANALYSIS_LIST.add("com.auth0.jwt.algorithms.Algorithm: com.auth0.jwt.algorithms.Algorithm RSA256(java.security.interfaces.RSAPublicKey,java.security.interfaces.RSAPrivateKey)");
        ANALYSIS_LIST.add("com.auth0.jwt.algorithms.Algorithm: com.auth0.jwt.algorithms.Algorithm RSA384(java.security.interfaces.RSAPublicKey,java.security.interfaces.RSAPrivateKey)");
        ANALYSIS_LIST.add("com.auth0.jwt.algorithms.Algorithm: com.auth0.jwt.algorithms.Algorithm RSA512(java.security.interfaces.RSAPublicKey,java.security.interfaces.RSAPrivateKey)");

    }

    @Override
    public void checkRule(
            SourceType type,
            List<String> projectPaths,
            List<String> projectDependencyPath,
            List<String> sourcePaths,
            OutputStructure output,
            String mainKlass,
            String androidHome,
            String javaHome,
            String jwtInfoPath) throws ExceptionHandler, IOException {
        JavajwtRsaApiSecondFinder javajwtRsaApiSecondFinder = new JavajwtRsaApiSecondFinder();
        javajwtRsaApiSecondFinder.checkRule(type, projectPaths, projectDependencyPath, sourcePaths, output, mainKlass, androidHome, javaHome, jwtInfoPath);
        javajwtRsaApiSecondFinder.getAnalysis_result();
        second_result = javajwtRsaApiSecondFinder.getAnalysis_result();
        if (second_result.isEmpty() || second_result == null) {
            log.info("not find the usage of the insecure rsa api in java jwt lib");
            return;
        }
        JavajwtRsaApiBaseFinder javajwtRsaApiBaseFinder = new JavajwtRsaApiBaseFinder();
        javajwtRsaApiBaseFinder.checkRule(type, projectPaths, projectDependencyPath, sourcePaths, output, mainKlass, androidHome, javaHome, jwtInfoPath);
        first_result = javajwtRsaApiBaseFinder.getAnalysis_result();
        if (first_result.isEmpty() || first_result == null) {
            log.info("not find the usage of the insecure rsa api in java jwt lib");
            return;
        }
        analyzeSlice(first_result, second_result);
        createAnalysisOutput(output);
    }

    private void analyzeSlice(List<Analysis> verify_result, List<Analysis> rsa_result) {
        for (Analysis analysis : verify_result) {
            if (analysis.getAnalysisResult().isEmpty()) {
                log.info("Analysis result is null");
                return;
            }
            for (int index = 0; index < analysis.getAnalysisResult().size(); index++) {
                UnitContainer e = analysis.getAnalysisResult().get(index);

                /**
                 * 处理Verify函数中的algorithm初始化方法
                 */
                if (e instanceof AssignInvokeUnitContainer && e.getUnit() instanceof JAssignStmt) {
                    for (String analysisMethod : ANALYSIS_LIST) {
                        if (e.toString().contains(analysisMethod)) {
                            analysis_result.add(new JavaJwtRsaApiResult(analysis.getMethodChain(), e, analysisMethod));
                        }
                    }
                }
            }
        }
        if ((!analysis_result.isEmpty()) && analysis_result != null) {
            for (int index = 0; index < analysis_result.size(); index++) {
                JavaJwtRsaApiResult currentResult = analysis_result.get(index);
                if (!rsa_result.isEmpty() && rsa_result != null) {
                    for (Analysis analysis : rsa_result) {
                        //如果当前语句中包含Analysis
                        if (analysis.getMethodChain().contains(currentResult.getAnalysisMethod())) {
                            for (int i = 0; i < analysis.getAnalysisResult().size(); i++) {
                                UnitContainer u = analysis.getAnalysisResult().get(i);
                                if (u.getUnit() instanceof JAssignStmt && u.getUnit().toString().contains("null")) {
                                    analysis_result.get(index).setRightUsage(true);
                                } else if (u instanceof AssignInvokeUnitContainer) {
                                    analysis_result.get(index).setRightUsage(false);
                                    analysis_result.get(index).getWrongUsage().put(u.toString(), u);
                                }
                            }
                        }
                    }

                }
            }
        }
        log.info("stop analysis result");
    }

    public void createAnalysisOutput(OutputStructure outputStructure) {
        Map<String, JavaJwtRsaApiResultOutput> outputs = new HashMap<>();
        if (analysis_result.isEmpty() || analysis_result == null) {
            log.info("not find java jwt lib RSA api usage");
        } else {
            for (JavaJwtRsaApiResult result : analysis_result) {
                if (result.isRightUsage()) {
                    log.info("right usage");
                } else {
                    JavaJwtRsaApiResultOutput output = new JavaJwtRsaApiResultOutput();
                    processInvokeInfo(result.getMethodChain(), output);
                    output.setWrongUsageInfo(result.getUnitContainer().getMethod());
                    output.setWrongUsageLine(result.getUnitContainer().getUnit().getJavaSourceStartLineNumber());
                    outputs.put(output.getInvokeMethodName(), output);
                }
            }
        }
        if (!outputs.isEmpty() && outputs != null) {
            log.info("Found java jwt lib RSA api wrong usage: " + outputs.size());
            Iterator<Map.Entry<String, JavaJwtRsaApiResultOutput>> it = outputs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, JavaJwtRsaApiResultOutput> output = it.next();
                log.info("Wrong usage info: " + output.getKey() + "-----" + output.getValue().getWrongUsageInfo() + output.getValue().getWrongUsageLine());
            }
        }
    }

    private static void processInvokeInfo(String methodChain, JavaJwtRsaApiResultOutput javaJwtRsaApiResultOutput) {
        Pattern invokeInfoPattern = Pattern.compile("[<](.+)[]]");
        Pattern invokeClassNamePattern = Pattern.compile("[<](.+)[:]");
        Pattern invokeMethodNamePattern = Pattern.compile("[ ](.+)[)]");
        Pattern invokeStatLinePattern = Pattern.compile("[\\[](.+)[]]");
        String invokeInfo = null;
        Matcher invokeInfoMatcher = invokeInfoPattern.matcher(methodChain);
        if (invokeInfoMatcher.find()) {
            invokeInfo = invokeInfoMatcher.group();
        }
        if (invokeInfo != null) {
            String invokeClassName = null;
            String invokeMethodName = null;
            String invokeLineNum = null;
            Matcher invokeClassNameMatcher = invokeClassNamePattern.matcher(invokeInfo);
            if (invokeClassNameMatcher.find()) {
                invokeClassName = invokeClassNameMatcher.group();
                invokeClassName = invokeClassName.split("<")[invokeClassName.split("<").length - 1];
            }
            Matcher invokeMethodNameMatcher = invokeMethodNamePattern.matcher(invokeInfo);
            if (invokeMethodNameMatcher.find())
                invokeMethodName = invokeMethodNameMatcher.group();
            Matcher invokeLineNumMatcher = invokeStatLinePattern.matcher(invokeInfo);
            if (invokeLineNumMatcher.find())
                invokeLineNum = invokeLineNumMatcher.group();
            javaJwtRsaApiResultOutput.setInvokeClassName(invokeClassName);
            javaJwtRsaApiResultOutput.setInvokeMethodName(invokeMethodName);
            javaJwtRsaApiResultOutput.setInvokeStateLine(invokeLineNum);

        }
    }


}
