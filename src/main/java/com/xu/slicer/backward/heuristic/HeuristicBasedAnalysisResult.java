package com.xu.slicer.backward.heuristic;

import com.xu.analyzer.backward.UnitContainer;
import com.xu.slicer.backward.property.PropertyAnalysisResult;
import soot.SootMethod;

import java.util.List;
import java.util.Map;

public class HeuristicBasedAnalysisResult {
    private String instruction;
    private SootMethod method;
    private List<UnitContainer> analysis;
    private Map<String, List<PropertyAnalysisResult>> propertyUseMap;

    /**
     * Constructor for HeuristicBasedAnalysisResult.
     *
     * @param instruction a {@link String} object.
     * @param method a {@link soot.SootMethod} object.
     * @param analysis a {@link List} object.
     * @param propertyUseMap a {@link Map} object.
     */
    public HeuristicBasedAnalysisResult(
            String instruction,
            SootMethod method,
            List<UnitContainer> analysis,
            Map<String, List<PropertyAnalysisResult>> propertyUseMap) {
        this.instruction = instruction;
        this.method = method;
        this.analysis = analysis;
        this.propertyUseMap = propertyUseMap;
    }

    /**
     * Getter for the field <code>instruction</code>.
     *
     * @return a {@link String} object.
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * Getter for the field <code>method</code>.
     *
     * @return a {@link soot.SootMethod} object.
     */
    public SootMethod getMethod() {
        return method;
    }

    /**
     * Getter for the field <code>analysis</code>.
     *
     * @return a {@link List} object.
     */
    public List<UnitContainer> getAnalysis() {
        return analysis;
    }

    /**
     * Getter for the field <code>propertyUseMap</code>.
     *
     * @return a {@link Map} object.
     */
    public Map<String, List<PropertyAnalysisResult>> getPropertyUseMap() {
        return propertyUseMap;
    }
}
