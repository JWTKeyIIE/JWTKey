package com.xu.analyzer.ruleCheckers.certChecker;

import com.xu.analyzer.backward.UnitContainer;

public class CertVerifyAnalysisResult {
    boolean isCertFromX5C;
    String x5cSootString;
    boolean isVerifyChain;
    boolean isVerifySig;
    boolean isVerifyDate;
    String methodChain;
    String analysisMethod;

    public CertVerifyAnalysisResult(String analysisMethod) {
        this.analysisMethod = analysisMethod;
        this.isCertFromX5C = false;
        this.isVerifyChain = false;
        this.isVerifyDate = false;
        this.isVerifySig = false;
    }

    public boolean isCertFromX5C() {
        return isCertFromX5C;
    }

    public void setCertFromX5C(boolean certFromX5C) {
        isCertFromX5C = certFromX5C;
    }

    public boolean isVerifyChain() {
        return isVerifyChain;
    }

    public void setVerifyChain(boolean verifyChain) {
        isVerifyChain = verifyChain;
    }

    public boolean isVerifySig() {
        return isVerifySig;
    }

    public void setVerifySig(boolean verifySig) {
        isVerifySig = verifySig;
    }

    public boolean isVerifyDate() {
        return isVerifyDate;
    }

    public void setVerifyDate(boolean verifyDate) {
        isVerifyDate = verifyDate;
    }

    public String getMethodChain() {
        return methodChain;
    }

    public void setMethodChain(String methodChain) {
        this.methodChain = methodChain;
    }

    public String getAnalysisMethod() {
        return analysisMethod;
    }

    public void setAnalysisMethod(String analysisMethod) {
        this.analysisMethod = analysisMethod;
    }

    public String getX5cSootString() {
        return x5cSootString;
    }

    public void setX5cSootString(String x5cSootString) {
        this.x5cSootString = x5cSootString;
    }
}
