package com.xu.slicer.forward;

import soot.Unit;

import java.util.List;

public class SlicingResult {
    private MethodCallSiteInfo callSiteInfo;
    private List<Unit> analysisResult;

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
    public List<Unit> getAnalysisResult() {
        return analysisResult;
    }

    /**
     * Setter for the field <code>analysisResult</code>.
     *
     * @param analysisResult a {@link List} object.
     */
    public void setAnalysisResult(List<Unit> analysisResult) {
        this.analysisResult = analysisResult;
    }
}
