package com.xu.analyzer.ruleCheckers.httpChecker;

import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.backward.AssignInvokeUnitContainer;
import com.xu.analyzer.backward.InvokeUnitContainer;
import com.xu.analyzer.backward.UnitContainer;
import com.xu.analyzer.ruleCheckers.Criteria;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;
import soot.ValueBox;
import soot.jimple.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class JavaJwtHttpFinder extends UnsecureHttpFinder {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(JavaJwtHttpFinder.class);

    private static final List<String> CRITERIA_CLASSES = new ArrayList<>();
    private static final List<String> HTTP_URL_PATTERN = new ArrayList<>();
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    private Map<UnitContainer, List<String>> predictableSourcMap = new HashMap<>();
    private Map<UnitContainer, List<String>> othersSourceMap = new HashMap<>();
    static {
        HTTP_URL_PATTERN.add("\"http:(.)*");
        HTTP_URL_PATTERN.add("\"http$");

        Criteria criteria0 = new Criteria();
        criteria0.setClassName("com.auth0.jwk.UrlJwkProvider");
        criteria0.setMethodName("void <init>(java.lang.String)");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("com.auth0.jwk.UrlJwkProvider");
        criteria1.setMethodName("void <init>(java.net.URL)");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("com.auth0.jwk.UrlJwkProvider");
        criteria2.setMethodName("void <init>(java.net.URL,java.lang.Integer,java.lang.Integer,java.net.Proxy)");
        criteria2.setParam(0);
        CRITERIA_LIST.add(criteria2);
        Criteria criteria3 = new Criteria();
        criteria3.setClassName("com.auth0.jwk.UrlJwkProvider");
        criteria3.setMethodName("void <init>(java.net.URL,java.lang.Integer,java.lang.Integer,java.net.Proxy,java.util.Map)");
        criteria3.setParam(0);
        CRITERIA_LIST.add(criteria3);
        Criteria criteria4 = new Criteria();
        criteria4.setClassName("com.auth0.jwk.UrlJwkProvider");
        criteria4.setMethodName("void <init>(java.net.URL,java.lang.Integer,java.lang.Integer)");
        criteria4.setParam(0);
        CRITERIA_LIST.add(criteria4);

//        CRITERIA_CLASSES.add("com.auth0.jwk.UrlJwkProvider");
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public List<String> getCriteriaClasses() {
        return CRITERIA_CLASSES;
    }

    @Override
    public String getJwtLibName() {
        return "java-jwt";
    }

//    @Override
    public void analyzeSlice(Analysis analysis) {
        if (analysis.getAnalysisResult().isEmpty()) {
            return;
        }

        for (UnitContainer e : analysis.getAnalysisResult()) {

            if (e instanceof AssignInvokeUnitContainer) {
                List<UnitContainer> resFromInside = ((AssignInvokeUnitContainer) e).getAnalysisResult();

                for (UnitContainer unit : resFromInside) {
                    checkForMatch(unit);
                }
            }

            if (e instanceof InvokeUnitContainer) {
                List<UnitContainer> resFromInside = ((InvokeUnitContainer) e).getAnalysisResult();

                for (UnitContainer unit : resFromInside) {
                    checkForMatch(unit);
                }
            }

            checkForMatchInternal(e);
        }

    }
    private void checkForMatch(UnitContainer e) {

        if (e instanceof AssignInvokeUnitContainer) {
            List<UnitContainer> resFromInside = ((AssignInvokeUnitContainer) e).getAnalysisResult();

            for (UnitContainer unit : resFromInside) {
                checkForMatch(unit);
            }
        }

        if (e instanceof InvokeUnitContainer) {
            List<UnitContainer> resFromInside = ((InvokeUnitContainer) e).getAnalysisResult();

            for (UnitContainer unit : resFromInside) {
                checkForMatch(unit);
            }
        }

        checkForMatchInternal(e);
    }

    private void checkForMatchInternal(UnitContainer e) {
        for (ValueBox usebox : e.getUnit().getUseBoxes()) {
            if (usebox.getValue() instanceof Constant) {
                boolean found = false;

                for (String regex : getPatternsToMatch()) {
                    if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                            .matcher(usebox.getValue().toString())
                            .matches()) {
                        putIntoMap(predictableSourcMap, e, usebox.getValue().toString());
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    putIntoMap(othersSourceMap, e, usebox.getValue().toString());
                }
            }
        }
    }

    public List<String> getPatternsToMatch() {
        return HTTP_URL_PATTERN;
    }
    public String getRuleId() {
        return "7";
    }

    @Override
    public void createAnalysisOutput(Map<String, String> xmlFileStr, List<String> sourcePaths, OutputStructure output) throws ExceptionHandler {
        Utils.createAnalysisOutput(xmlFileStr,sourcePaths,predictableSourcMap,"7",output,"java-jwt");
    }
}
