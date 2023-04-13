package com.xu.analyzer.ruleCheckers;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.BaseAnalyzerRouting;
import com.xu.analyzer.backward.UnitContainer;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class BaseRuleChecker implements RuleChecker{
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(BaseRuleChecker.class);
//    public static final List<String> CRITERIA_CLASSES = new ArrayList<>();

    @Override
    public void checkRule(SourceType type,
                          List<String> projectPaths,
                          List<String> projectDependencyPath,
                          List<String> sourcePaths,
                          OutputStructure output,
                          String mainKlass,
                          String androidHome,
                          String javaHome,
                          String jwtInfoPath)
            throws ExceptionHandler, IOException {
        String[] excludes = {"web.xml", "pom.xml"};
        Map<String, String> xmlFileStr =
                Utils.getXmlFiles(projectPaths.get(0), Arrays.asList(excludes));
        for(Criteria criteria : getCriteriaList()) {
            //environmentRouting 用于传递参数并且选择处理方式的函数
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
        createAnalysisOutput(xmlFileStr, sourcePaths, output);
    }
    public abstract List<Criteria> getCriteriaList();

    public abstract List<String> getCriteriaClasses();

    public abstract void analyzeSlice(Analysis analysis);

    public abstract void createAnalysisOutput(
            Map<String, String> xmlFileStr, List<String> sourcePaths, OutputStructure output)
            throws ExceptionHandler;

    protected void putIntoMap(
            Map<UnitContainer, List<String>> unitStringMap, UnitContainer e, String value) {
        List<String> values = unitStringMap.get(e);
        if(values == null) {
            values = new ArrayList<>();
            values.add(value);
            unitStringMap.put(e, values);
            return;
        }
        if(!values.toString().contains(value)){
            values.add(value);
        }
    }
}
