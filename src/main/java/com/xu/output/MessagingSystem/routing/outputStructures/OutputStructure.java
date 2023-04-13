package com.xu.output.MessagingSystem.routing.outputStructures;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.output.AnalysisIssue;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

/**
 * 定义抽象类，用来描述输出的格式
 */
public abstract class OutputStructure {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(OutputStructure.class);

    private EnvironmentInfo source;
    private File outfile;
    private SourceType type;
    private final HashMap<Integer, Integer> countOfBugs = new HashMap<>();
    private final ArrayList<AnalysisIssue> collection;
    private final Function<AnalysisIssue, String> errorAddition;
    private final Function<HashMap<Integer, Integer>, String> bugSummaryHandler;
    private final Charset chars = StandardCharsets.UTF_8;

    public OutputStructure(EnvironmentInfo info) {
        this.source = info;
        this.outfile = new File(info.getFileOut());
        this.type = info.getSourceType();
        this.collection = new ArrayList<>();
        this.errorAddition = info.getErrorAddition();
        this.bugSummaryHandler = info.getBugSummaryHandler();
    }

    /** Constructor for OutputStructure. */
    public OutputStructure() {
        this.collection = new ArrayList<>();
        this.errorAddition = null;
        this.bugSummaryHandler = null;
    }
    //endregion

    //region Methods to be overridden

    /**
     * startAnalyzing.
     *
     * @throws ExceptionHandler if any.
     */
    public abstract void startAnalyzing() throws ExceptionHandler;

    private void addIssueCore(AnalysisIssue issue) throws ExceptionHandler {
        if (this.errorAddition != null) log.info(this.errorAddition.apply(issue));

        log.debug("Adding Issue: " + issue.getInfo());
        //Keeping a rolling count of the different kinds of bugs occuring
        if (!countOfBugs.containsKey(issue.getRuleId())) {
            countOfBugs.put(issue.getRuleId(), 1);
        } else {
            countOfBugs.put(issue.getRuleId(), countOfBugs.get(issue.getRuleId()) + 1);
        }
    }

    /**
     * addIssue.
     *
     * @param issue a {@link AnalysisIssue} object.
     * @throws ExceptionHandler if any.
     */
    public void addIssue(AnalysisIssue issue) throws ExceptionHandler {
        this.addIssueCore(issue);
    }

    /**
     * addIssueToCollection.
     *
     * @param issue a {@link AnalysisIssue} object.
     * @throws ExceptionHandler if any.
     */
    public void addIssueToCollection(AnalysisIssue issue) throws ExceptionHandler {
        this.addIssueCore(issue);
        this.collection.add(issue);
    }

    /**
     * stopAnalyzing.
     *
     * @throws ExceptionHandler if any.
     */
    public abstract void stopAnalyzing() throws ExceptionHandler;
    //endregion

    //region Public helper methods

    /**
     * createBugCategoryList.
     *
     * @return a {@link BugSummary} object.
     */
/*    public BugSummary createBugCategoryList() {
        log.debug("Creating the Bug Summary");

        BugSummary bugDict = new BugSummary();
        //region Creating A Bug Category with counts per the Broken Rules
        for (int ruleNumber : countOfBugs.keySet()) {
            BugCategory ruleType = new BugCategory();

            ruleType.setGroup(RuleList.getRuleByRuleNumber(ruleNumber).getDesc());
            ruleType.setCode(String.valueOf(ruleNumber));
            ruleType.setCount(countOfBugs.get(ruleNumber));

            if (countOfBugs.get(ruleNumber) > 0) bugDict.addBugSummary(ruleType);

            log.debug("Added ruleType: " + ruleType.toString());
        }
        //endregion

        if (this.bugSummaryHandler != null) log.info(this.bugSummaryHandler.apply(countOfBugs));

        return bugDict;
    }*/

    /**
     * Getter for the field <code>source</code>.
     *
     * @return a {@link EnvironmentInfo} object.
     */
    public EnvironmentInfo getSource() {
        return source;
    }

    /**
     * Getter for the field <code>collection</code>.
     *
     * @return a {@link ArrayList} object.
     */
    public ArrayList<AnalysisIssue> getCollection() {
        return collection;
    }

    /**
     * Getter for the field <code>outfile</code>.
     *
     * @return a {@link File} object.
     */
    public File getOutfile() {
        return outfile;
    }

    /**
     * Getter for the field <code>type</code>.
     *
     * @return a {@link SourceType} object.
     */
    public SourceType getType() {
        return type;
    }

    /**
     * Getter for the field <code>chars</code>.
     *
     * @return a {@link Charset} object.
     */
    public Charset getChars() {
        return chars;
    }

    /**
     * Getter for the field <code>countOfBugs</code>.
     *
     * @return a {@link HashMap} object.
     */
    public HashMap<Integer, Integer> getCountOfBugs() {
        return countOfBugs;
    }

    /**
     * Getter for the field <code>cwes</code>.
     *
     * @return a {@link CWEList} object.
     *//*
    public CWEList getCwes() {
        return cwes;
    }*/

    /**
     * Setter for the field <code>source</code>.
     *
     * @param source a {@link EnvironmentInfo} object.
     */
    public void setSource(EnvironmentInfo source) {
        this.source = source;
    }

    /**
     * Setter for the field <code>outfile</code>.
     *
     * @param outfile a {@link File} object.
     */
    public void setOutfile(File outfile) {
        this.outfile = outfile;
    }

    /**
     * Setter for the field <code>type</code>.
     *
     * @param type a {@link SourceType} object.
     */
    public void setType(SourceType type) {
        this.type = type;
    }

}
