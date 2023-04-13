package com.xu.slicer.backward.method;

import com.xu.analyzer.backward.UnitContainer;
import com.xu.slicer.backward.MethodCallSiteInfo;
import com.xu.slicer.backward.property.PropertyAnalysisResult;

import java.util.List;
import java.util.Map;

public class MethodSlicingResult {
    private MethodCallSiteInfo callSiteInfo;
    private List<Integer> influencingParameters;
    private List<UnitContainer> analysisResult;
    private Map<String, List<PropertyAnalysisResult>> propertyUseMap;


    public MethodCallSiteInfo getCallSiteInfo() {
        return callSiteInfo;
    }

    public void setCallSiteInfo(MethodCallSiteInfo callSiteInfo) {
        this.callSiteInfo = callSiteInfo;
    }

    public List<Integer> getInfluencingParameters() {
        return influencingParameters;
    }

    public void setInfluencingParameters(List<Integer> influencingParameters) {
        this.influencingParameters = influencingParameters;
    }

    public List<UnitContainer> getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(List<UnitContainer> analysisResult) {
        this.analysisResult = analysisResult;
    }

    public Map<String, List<PropertyAnalysisResult>> getPropertyUseMap() {
        return propertyUseMap;
    }

    public void setPropertyUseMap(Map<String, List<PropertyAnalysisResult>> propertyUseMap) {
        this.propertyUseMap = propertyUseMap;
    }
}
