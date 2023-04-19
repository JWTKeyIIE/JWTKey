package com.xu.analyzer.ruleCheckers.javajwt;

import com.xu.analyzer.BaseAnalyzerRouting;
import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.ruleCheckers.BaseRuleChecker;
import com.xu.analyzer.ruleCheckers.Criteria;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//进行逆向和正向分析
public class JavajwtRsaApiSecondFinder extends BaseRuleChecker {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(JavajwtRsaApiSecondFinder.class);
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private static final List<String> ANALYSIS_LIST = new ArrayList<>();
    public static final List<Analysis> analysis_result = new ArrayList<>();

    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("com.auth0.jwt.algorithms.Algorithm");
        criteria0.setMethodName("com.auth0.jwt.algorithms.Algorithm RSA256(java.security.interfaces.RSAPublicKey,java.security.interfaces.RSAPrivateKey)");
        criteria0.setParam(1);
        CRITERIA_LIST.add(criteria0);
/*        Criteria criteria1 = new Criteria();
        criteria1.setClassName("com.auth0.jwt.JWT");
        criteria1.setMethodName("com.auth0.jwt.interfaces.Verification require(com.auth0.jwt.algorithms.Algorithm)");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);*/

/*        CRITERIA_CLASSES.add("com.auth0.jwt.JWT");
        CRITERIA_CLASSES.add("com.auth0.jwt.JWTVerifier");
        CRITERIA_CLASSES.add("com.auth0.jwt.algorithms.Algorithm");
        CRITERIA_CLASSES.add("com.auth0.jwt.JWTCreator");
        CRITERIA_CLASSES.add("java.io.IOException");
        CRITERIA_CLASSES.add("java.security.PrivateKey");
        CRITERIA_CLASSES.add("java.security.PublicKey");
        CRITERIA_CLASSES.add("java.security.interfaces.RSAPrivateKey");
        CRITERIA_CLASSES.add("java.security.interfaces.RSAPublicKey");*/
/*        CRITERIA_CLASSES.add("java.util.Date");
        CRITERIA_CLASSES.add("java.util.List");
        CRITERIA_CLASSES.add("java.util.Map");
        CRITERIA_CLASSES.add("com.cztx.common.myenum.MyEnum");*/
/*        CRITERIA_CLASSES.add("com.auth0.jwt.JWT");
        CRITERIA_CLASSES.add("com.auth0.jwt.JWTVerifier");
        CRITERIA_CLASSES.add("com.auth0.jwt.algorithms.Algorithm");
        CRITERIA_CLASSES.add("com.auth0.jwt.JWTCreator");
        CRITERIA_CLASSES.add("com.auth0.jwt.interfaces.DecodedJWT");

        CRITERIA_CLASSES.add("com.auth0.jwt.exceptions.JWTVerificationException");
        CRITERIA_CLASSES.add("java.io.IOException");
        CRITERIA_CLASSES.add("java.security.KeyFactory");
        CRITERIA_CLASSES.add("java.security.spec.X509EncodedKeySpec");

        CRITERIA_CLASSES.add("java.io.IOException");
        CRITERIA_CLASSES.add("java.security.PrivateKey");
        CRITERIA_CLASSES.add("java.security.PublicKey");
        CRITERIA_CLASSES.add("java.security.interfaces.RSAPrivateKey");
        CRITERIA_CLASSES.add("java.security.interfaces.RSAPublicKey");*/

        ANALYSIS_LIST.add("com.auth0.jwt.algorithms.Algorithm: com.auth0.jwt.algorithms.Algorithm RSA256(java.security.interfaces.RSAPublicKey,java.security.interfaces.RSAPrivateKey)");
        ANALYSIS_LIST.add("com.auth0.jwt.algorithms.Algorithm: com.auth0.jwt.algorithms.Algorithm RSA384(java.security.interfaces.RSAPublicKey,java.security.interfaces.RSAPrivateKey)");
        ANALYSIS_LIST.add("com.auth0.jwt.algorithms.Algorithm: com.auth0.jwt.algorithms.Algorithm RSA512(java.security.interfaces.RSAPublicKey,java.security.interfaces.RSAPrivateKey)");


    }

    public JavajwtRsaApiSecondFinder() {
    }

    @Override
    public void checkRule(SourceType type, List<String> projectPaths, List<String> projectDependencyPath, List<String> sourcePaths, OutputStructure output, String mainKlass, String androidHome, String javaHome, String jwtInfoPath) throws ExceptionHandler, IOException {
        String[] excludes = {"web.xml", "pom.xml"};
        Map<String, String> xmlFileStr =
                Utils.getXmlFiles(projectPaths.get(0), Arrays.asList(excludes));
        for(Criteria criteria : getCriteriaList()){
            BaseAnalyzerRouting.environmentRouting(
                    type,
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
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public void analyzeSlice(Analysis analysis) {
        log.info("Add RSA Algorithm API Analysis result");
        analysis_result.add(analysis);
/*        if (analysis.getAnalysisResult().isEmpty()) {
            log.info("Analysis result is null");
        }
        log.info("Begin to analysis result for JavaJwtRsaApiFinder");

        for(int index = 0; index < analysis.getAnalysisResult().size(); index ++) {
            UnitContainer e = analysis.getAnalysisResult().get(index);

            for(String analysisMethod : ANALYSIS_LIST){
                if(e.toString().contains(analysisMethod) && e instanceof AssignInvokeUnitContainer){
                    List<UnitContainer> getAnalysisResult = ((AssignInvokeUnitContainer)e).getAnalysisResult();

                }

            }

        }*/

    }

    @Override
    public void createAnalysisOutput(Map<String, String> xmlFileStr, List<String> sourcePaths, OutputStructure output) throws ExceptionHandler {
        log.info("Java jwt api Finder Second Step");
    }

    public List<Analysis> getAnalysis_result() {
        return analysis_result;
    }

}
