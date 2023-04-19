package com.xu.output;

import com.xu.analyzer.backward.UnitContainer;
import com.xu.analyzer.ruleCheckers.RuleList;
import com.xu.analyzer.ruleCheckers.springProject.SpringFieldValueInfo;
import com.xu.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AnalysisIssue {
    private String fullPathName;
    private String className;
    private RuleList rule;
    private Stack methods;
    private ArrayList<AnalysisLocation> locations;
    private String info;
    private String jwtLibName;

    public AnalysisIssue(
            String sootString, Integer ruleNumber, String Info, List<String> sourcePaths, String jwtLibName) {
        String className = Utils.retrieveClassNameFromSootString(sootString);
        String methodName = Utils.retrieveMethodFromSootString(sootString);
        Integer lineNum = Utils.retrieveLineNumFromSootString(sootString);
        String constant = null;

        if (sootString.contains("constant keys") || ruleNumber == 3)
            constant = Utils.retrieveFoundMatchFromSootString(sootString);

        if (lineNum >= 0) this.addMethod(methodName, new AnalysisLocation(lineNum));
        else this.addMethod(methodName);

        this.className = className;
        this.rule = RuleList.getRuleByRuleNumber(ruleNumber);

        if (constant != null) Info += " Found value \"" + constant + "\"";

        this.info = Info;

        this.fullPathName = getPathFromSource(sourcePaths, className);
        this.jwtLibName = jwtLibName;
    }
    public AnalysisIssue(SpringFieldValueInfo springFieldValueInfo){
        if(springFieldValueInfo.getSpringPropertyFindResult()!= null){
           if(springFieldValueInfo.getSpringPropertyFindResult().getProblemType().contains("constant key")){
               this.addMethod("properties file");
               this.fullPathName = "properties file";
//               this.className = springFieldValueInfo.getSpringPropertyFindResult().getPropertyName();
               this.rule = RuleList.getRuleByRuleNumber(4);
               this.info = RuleList.getRuleByRuleNumber(4).getDesc()+". property name: " + springFieldValueInfo.getSpringPropertyFindResult().getPropertyName()
                       +" property value: " + springFieldValueInfo.getSpringPropertyFindResult().getPropertyValue();
           }
           if(springFieldValueInfo.getSpringPropertyFindResult().getProblemType().contains("http")){
               this.addMethod("properties file");
               this.fullPathName = "properties file";
//               this.className = springFieldValueInfo.getSpringPropertyFindResult().getPropertyName();
               this.rule = RuleList.getRuleByRuleNumber(7);
               this.info = RuleList.getRuleByRuleNumber(7).getDesc()+". property name: " + springFieldValueInfo.getSpringPropertyFindResult().getPropertyName()
                       +" property value: " + springFieldValueInfo.getSpringPropertyFindResult().getPropertyValue();
           }
           if(springFieldValueInfo.getSpringPropertyFindResult().getProblemType().contains("private")){
               this.addMethod("properties file");
               this.fullPathName = "properties file";
//               this.className = springFieldValueInfo.getSpringPropertyFindResult().getPropertyName();
               this.rule = RuleList.getRuleByRuleNumber(6);
               this.info = RuleList.getRuleByRuleNumber(6).getDesc() +
                       ". property name: " + springFieldValueInfo.getSpringPropertyFindResult().getPropertyName()
                       + " property value: " + springFieldValueInfo.getSpringPropertyFindResult().getPropertyValue();
           }
        }
    }

    /**
     * Constructor for AnalysisIssue.
     *
     * @param unit a {@link UnitContainer} object.
     * @param ruleNumber a {@link Integer} object.
     * @param sootString a {@link String} object.
     * @param sourcePaths a {@link List} object.
     */
    public AnalysisIssue(
            UnitContainer unit, Integer ruleNumber, String sootString, List<String> sourcePaths, String jwtLibName) {
        Integer lineNum;
        String constant = null;

        if (sootString.contains("constant keys") || ruleNumber == 3)
            constant = Utils.retrieveFoundMatchFromSootString(sootString);

        String methodName = Utils.retrieveMethodFromSootString(unit.getMethod());

        if ((lineNum = unit.getUnit().getJavaSourceStartLineNumber()) >= 0) {
            AnalysisLocation tempLoc = new AnalysisLocation(lineNum);
            if (unit.getUnit().getJavaSourceStartColumnNumber() >= 0) {
                tempLoc.setColStart(unit.getUnit().getJavaSourceStartColumnNumber());
                tempLoc.setColEnd(unit.getUnit().getJavaSourceStartColumnNumber());
            }

            this.addMethod(methodName, tempLoc);
        }

        this.className = Utils.retrieveClassNameFromSootString(unit.getMethod());
        this.rule = RuleList.getRuleByRuleNumber(ruleNumber);

        this.info = Utils.retrieveFoundPatternFromSootString(sootString);

        if (this.info.equals("UNKNOWN") && constant != null)
            this.info = "Found: Constant \"" + constant + "\"";
        else if (this.info.equals("UNKNOWN") && constant == null) this.info = "Found: " + sootString;
        else if (constant != null) this.info += " Found value \"" + constant + "\"";
        else this.info = "Found: \"" + this.info + "\"";

        this.info = this.info.replace("Found: Found: ", "Found: ");

        if (lineNum <= 0) {
            this.addMethod(
                    methodName, new AnalysisLocation(Utils.retrieveLineNumFromSootString(sootString)));
        }

        if (this.getMethods().empty()) this.addMethod(methodName);

        this.fullPathName = getPathFromSource(sourcePaths, className);
        this.jwtLibName = jwtLibName;
    }

    //endregion

    //region Helper Methods

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append("ClassName: ").append(this.className).append(", ");
        out.append("Rule: ").append(this.rule).append(", ");
        out.append("Methods: ").append(this.methods.peek().toString()).append(", ");
        if (getLocations().size() > 0) {
            out.append("Locations: ");
            getLocations().stream().forEach(loc -> out.append(loc.toString()).append(" && "));
        } else out.append("No Locations");
        out.append(", ");
        out.append("Info: ").append(this.info);

        return out.toString();
    }

    /**
     * getPathFromSource.
     *
     * @param sources a {@link List} object.
     * @param className a {@link String} object.
     * @return a {@link String} object.
     */
    public String getPathFromSource(List<String> sources, String className) {
        String relativePath = null;
        if (sources.size() == 1) {
            String fullSource = sources.get(0);
            String[] split = fullSource.split(Utils.fileSep);
            fullSource = split[split.length - 1];

            if (fullSource.endsWith(":dir"))
                relativePath =
                        Utils.osPathJoin(
                                fullSource.replace(":dir", ""),
                                "src",
                                "main",
                                "java",
                                className.replace(".", System.getProperty("file.separator")) + ".java");
            else
                relativePath =
                        Utils.osPathJoin(
                                fullSource,
                                className.replace(".", System.getProperty("file.separator")) + ".class");
        } else {
            for (String in : sources)
                if (in.contains(className)) {
                    //String[] split = in.split(Utils.fileSep);
                    //relativePath = split[split.length - 1];
                    relativePath = in;
                } else if (in.contains(className.replace(".", Utils.fileSep))) {
                    //String[] split = in.split(Utils.fileSep);
                    //relativePath = split[split.length - 1];
                    relativePath = in;
                }
        }
        if (relativePath == null) return "UNKNOWN";
        else return relativePath;
    }
    //endregion

    //region Getters/Setters

    /**
     * Getter for the field <code>fullPathName</code>.
     *
     * @return a {@link String} object.
     */
    public String getFullPathName() {
        return fullPathName;
    }

    /**
     * Setter for the field <code>fullPathName</code>.
     *
     * @param fullPathName a {@link String} object.
     */
    public void setFullPathName(String fullPathName) {
        this.fullPathName = fullPathName;
    }

    /**
     * Getter for the field <code>className</code>.
     *
     * @return a {@link String} object.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Setter for the field <code>className</code>.
     *
     * @param className a {@link String} object.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Getter for the field <code>rule</code>.
     *
     * @return a {@link RuleList} object.
     */
    public RuleList getRule() {
        return rule;
    }

    /**
     * getRuleId.
     *
     * @return a {@link Integer} object.
     */
    public Integer getRuleId() {
        return rule.getRuleId();
    }

    /**
     * Getter for the field <code>methods</code>.
     *
     * @return a {@link Stack} object.
     */
    public Stack getMethods() {

        if (this.methods == null) {
            this.methods = new Stack();
        }

        return this.methods;
    }

    /**
     * Getter for the field <code>locations</code>.
     *
     * @return a {@link ArrayList} object.
     */
    public ArrayList<AnalysisLocation> getLocations() {

        if (this.locations == null) {
            this.locations = new ArrayList<>();
        }

        return locations;
    }

    /**
     * addLocation.
     *
     * @param newLocation a {@link AnalysisLocation} object.
     */
    public void addLocation(AnalysisLocation newLocation) {
        this.getLocations().add(newLocation);
    }

    /**
     * addMethod.
     *
     * @param methodName a {@link String} object.
     */
    public void addMethod(String methodName) {
        this.getMethods().push(String.valueOf(methodName));
    }

    /**
     * addMethod.
     *
     * @param methodName a {@link String} object.
     * @param location a {@link AnalysisLocation} object.
     */
    public void addMethod(String methodName, AnalysisLocation location) {
        location.setMethodNumber(this.getMethods().size());
        this.getMethods().push(String.valueOf(methodName));

        this.addLocation(location);
    }

    /**
     * Getter for the field <code>info</code>.
     *
     * @return a {@link String} object.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Setter for the field <code>info</code>.
     *
     * @param info a {@link String} object.
     */
    public void setInfo(String info) {
        this.info = info;
    }
    //endregion

}
