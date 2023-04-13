package com.xu.analyzer.ruleCheckers.javajwt;

import com.xu.analyzer.backward.UnitContainer;

import java.util.HashMap;
import java.util.Map;

public class JavaJwtRsaApiResult {
    String methodChain;
    UnitContainer unitContainer;
    String analysisMethod;
    boolean rightUsage = true;
    Map<String, UnitContainer> wrongUsage = new HashMap<>();


    public JavaJwtRsaApiResult(String methodChain, UnitContainer unitContainer, String analysisMethod) {
        this.methodChain = methodChain;
        this.unitContainer = unitContainer;
        this.analysisMethod = analysisMethod;
    }

    public String getMethodChain() {
        return methodChain;
    }

    public void setMethodChain(String methodChain) {
        this.methodChain = methodChain;
    }

    public UnitContainer getUnitContainer() {
        return unitContainer;
    }

    public void setUnitContainer(UnitContainer unitContainer) {
        this.unitContainer = unitContainer;
    }

    public String getAnalysisMethod() {
        return analysisMethod;
    }

    public void setAnalysisMethod(String analysisMethod) {
        analysisMethod = analysisMethod;
    }

    public void setRightUsage(boolean rightUsage) {
        this.rightUsage = rightUsage;
    }

    public boolean isRightUsage() {
        return rightUsage;
    }

    public Map<String, UnitContainer> getWrongUsage() {
        return wrongUsage;
    }

    public void setWrongUsage(Map<String, UnitContainer> wrongUsage) {
        this.wrongUsage = wrongUsage;
    }
}
