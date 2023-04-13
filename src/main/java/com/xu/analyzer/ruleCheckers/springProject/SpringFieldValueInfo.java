package com.xu.analyzer.ruleCheckers.springProject;

public class SpringFieldValueInfo {
    private String fieldName;
    private String filedType;
    private String annotations;
    private Boolean hasFindProblem;
    private SpringPropertyFindResult springPropertyFindResult;

    public SpringFieldValueInfo(String fieldName, String filedType, String annotations) {
        this.fieldName = fieldName;
        this.filedType = filedType;
        this.annotations = annotations;
        this.hasFindProblem = false;
    }

    public SpringFieldValueInfo(SpringPropertyFindResult springPropertyFindResult) {
        this.hasFindProblem = true;
        this.springPropertyFindResult = springPropertyFindResult;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFiledType() {
        return filedType;
    }

    public void setFiledType(String filedType) {
        this.filedType = filedType;
    }

    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public Boolean getHasFindProblem() {
        return hasFindProblem;
    }

    public void setHasFindProblem(Boolean hasFindProblem) {
        this.hasFindProblem = hasFindProblem;
    }

    public SpringPropertyFindResult getSpringPropertyFindResult() {
        return springPropertyFindResult;
    }

    public void setSpringPropertyFindResult(SpringPropertyFindResult springPropertyFindResult) {
        this.springPropertyFindResult = springPropertyFindResult;
    }
}
