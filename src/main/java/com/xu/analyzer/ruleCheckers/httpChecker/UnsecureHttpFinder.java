package com.xu.analyzer.ruleCheckers.httpChecker;

import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.backward.AssignInvokeUnitContainer;
import com.xu.analyzer.backward.InvokeUnitContainer;
import com.xu.analyzer.backward.UnitContainer;
import com.xu.analyzer.ruleCheckers.BaseRuleChecker;
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

public abstract class UnsecureHttpFinder extends BaseRuleChecker {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(UnsecureHttpFinder.class);
    private static final List<String> HTTP_URL_PATTERN = new ArrayList<>();
    static {
        HTTP_URL_PATTERN.add("\"http:(.)*");
        HTTP_URL_PATTERN.add("\"http$");
    }

    private final String rule = getRuleId();
    private final String jwtLibName = getJwtLibName();
    private Map<UnitContainer, List<String>> predictableSourceMap = new HashMap<>();
    private Map<UnitContainer, List<String>> othersSourceMap = new HashMap<>();

    @Override
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
                        putIntoMap(predictableSourceMap, e, usebox.getValue().toString());
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


    @Override
    public void createAnalysisOutput(Map<String, String> xmlFileStr, List<String> sourcePaths, OutputStructure output) throws ExceptionHandler {
        Utils.createAnalysisOutput(xmlFileStr,sourcePaths,predictableSourceMap,rule,output,"java-jwt");
    }

    public String getRuleId() {
        return "7";
    }

    public abstract String getJwtLibName();

    public abstract List<String> getCriteriaClasses();
}
