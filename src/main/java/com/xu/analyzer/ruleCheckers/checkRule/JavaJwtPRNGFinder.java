package com.xu.analyzer.ruleCheckers.checkRule;

import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.ruleCheckers.analysisEntry.JavaJwtEntry;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaJwtPRNGFinder extends JavaJwtEntry {
    private static final List<Analysis> javaJWTEntrySlice = new ArrayList<>();
    @Override
    public void analyzeSlice(Analysis analysis) {
        javaJWTEntrySlice.add(analysis);
    }

    @Override
    public void createAnalysisOutput(Map<String, String> xmlFileStr, List<String> sourcePaths, OutputStructure output) throws ExceptionHandler {

    }
}
