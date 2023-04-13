package com.xu.analyzer.backward;

import java.util.List;

public class Analysis {
    private String methodChain;
    private List<UnitContainer> analysisResult;

    /**
     * Getter for the field <code>methodChain</code>.
     *
     * @return a {@link String} object.
     */
    public String getMethodChain() {
        return methodChain;
    }

    /**
     * Setter for the field <code>methodChain</code>.
     *
     * @param methodChain a {@link String} object.
     */
    public void setMethodChain(String methodChain) {
        this.methodChain = methodChain;
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
}
