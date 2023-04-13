package com.xu.analyzer.ruleCheckers.springProject;

public class SpringPropertyFindResult {
    String problemType; // constant symmetric key, http, plaintext asymmetric key
    String propertyName;
    String propertyValue;
    String defaultValue;

    public SpringPropertyFindResult(String problemType) {
        this.problemType = problemType;
    }

    public SpringPropertyFindResult(String problemType, String propertyName, String propertyValue) {
        this.problemType = problemType;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public String getProblemType() {
        return problemType;
    }

    public void setProblemType(String problemType) {
        this.problemType = problemType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
