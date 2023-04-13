package com.xu.slicer.backward.orthogonal;

import com.xu.analyzer.backward.UnitContainer;
import com.xu.slicer.backward.MethodCallSiteInfo;
import com.xu.slicer.backward.property.PropertyAnalysisResult;

import java.util.List;
import java.util.Map;

public class OrthogonalSlicingResult {

    private MethodCallSiteInfo callSiteInfo;
    private List<UnitContainer> analysisResult;
    private Map<String, List<PropertyAnalysisResult>> propertyUseMap;

    /**
     * Getter for the field <code>callSiteInfo</code>.
     *
     * @return a {@link MethodCallSiteInfo} object.
     */
    public MethodCallSiteInfo getCallSiteInfo() {
        return callSiteInfo;
    }

    /**
     * Setter for the field <code>callSiteInfo</code>.
     *
     * @param callSiteInfo a {@link MethodCallSiteInfo} object.
     */
    public void setCallSiteInfo(MethodCallSiteInfo callSiteInfo) {
        this.callSiteInfo = callSiteInfo;
    }

    /**
     * Getter for the field <code>analysisResult</code>.
     *
     * @return a {@link List} object.
     */
    public List<UnitContainer> getAnalysisResult() {
        return analysisResult;
    }

    /**
     * Setter for the field <code>analysisResult</code>.
     *
     * @param analysisResult a {@link List} object.
     */
    public void setAnalysisResult(List<UnitContainer> analysisResult) {
        this.analysisResult = analysisResult;
    }

    /**
     * Getter for the field <code>propertyUseMap</code>.
     *
     * @return a {@link Map} object.
     */
    public Map<String, List<PropertyAnalysisResult>> getPropertyUseMap() {
        return propertyUseMap;
    }

    /**
     * Setter for the field <code>propertyUseMap</code>.
     *
     * @param propertyUseMap a {@link Map} object.
     */
    public void setPropertyUseMap(Map<String, List<PropertyAnalysisResult>> propertyUseMap) {
        this.propertyUseMap = propertyUseMap;
    }

}
