package com.xu.analyzer.ruleCheckers.checkRule;


import com.xu.analyzer.UniqueRuleAnalyzer;
import com.xu.analyzer.ruleCheckers.RuleChecker;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import frontEnd.MessagingSystem.AnalysisIssue;
import soot.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.io.IOException;
import java.util.*;

public class InsecurePRNGFinder implements RuleChecker {
    private static final List<String> UNTRUSTED_PRNGS = new ArrayList<>();

    static {
        UNTRUSTED_PRNGS.add("java.util.Random: void <init>");
        UNTRUSTED_PRNGS.add("java.lang.Math: double random");
    }

    private static Map<String, List<Unit>> getUntrustedPrngInstructions(List<String> classNames) {

        Map<String, List<Unit>> analysisList = new HashMap<>();

        for (String className : classNames) {
            SootClass sClass = Scene.v().loadClassAndSupport(className);

            for (SootMethod method : sClass.getMethods()) {
                if (method.isConcrete()) {

                    List<Unit> analysis = new ArrayList<>();

                    Body b = method.retrieveActiveBody();
                    DirectedGraph g = new ExceptionalUnitGraph(b);
                    Iterator gitr = g.iterator();
                    while (gitr.hasNext()) {
                        Unit unit = (Unit) gitr.next();

                        for (String prng : UNTRUSTED_PRNGS) {
                            if (unit.toString().contains(prng)) {
                                analysis.add(unit);
                            }
                        }
                    }
                    analysisList.put(method.toString(), analysis);
                }
            }
        }

        return analysisList;
    }
    @Override
    public void checkRule(SourceType type, List<String> projectJarPath, List<String> projectDependencyPath, List<String> sourcePaths, OutputStructure output, String mainKlass, String androidHome, String javaHome, String jwtInfoPath) throws ExceptionHandler, IOException {

        Map<String, List<Unit>> analysisLists =
                getUntrustedPrngInstructions(
                        UniqueRuleAnalyzer.environmentRouting(
                                projectJarPath, projectDependencyPath, type, androidHome, javaHome));

        if (!analysisLists.isEmpty()) {
            for (String method : analysisLists.keySet()) {
                List<Unit> analysis = analysisLists.get(method);

                if (!analysis.isEmpty()) {

                }
            }
        }
    }
}
