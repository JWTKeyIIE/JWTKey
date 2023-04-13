package com.xu.analyzer.ruleCheckers.javajwt;

public class JavaJwtRsaApiResultOutput {
    public String invokeClassName = "Unknown";
    public String invokeMethodName = "Unknown";
    public String invokeStateLine = "Unknown";
    public String wrongUsageInfo = "Unknown";
    public int wrongUsageLine;

    public JavaJwtRsaApiResultOutput() {
    }

    public String getInvokeClassName() {
        return invokeClassName;
    }

    public void setInvokeClassName(String invokeClassName) {
        this.invokeClassName = invokeClassName;
    }

    public String getInvokeMethodName() {
        return invokeMethodName;
    }

    public void setInvokeMethodName(String invokeMethodName) {
        this.invokeMethodName = invokeMethodName;
    }

    public String getInvokeStateLine() {
        return invokeStateLine;
    }

    public void setInvokeStateLine(String invokeStateLine) {
        this.invokeStateLine = invokeStateLine;
    }

    public String getWrongUsageInfo() {
        return wrongUsageInfo;
    }

    public void setWrongUsageInfo(String wrongUsageInfo) {
        this.wrongUsageInfo = wrongUsageInfo;
    }

    public int getWrongUsageLine() {
        return wrongUsageLine;
    }

    public void setWrongUsageLine(int wrongUsageLine) {
        this.wrongUsageLine = wrongUsageLine;
    }
}
