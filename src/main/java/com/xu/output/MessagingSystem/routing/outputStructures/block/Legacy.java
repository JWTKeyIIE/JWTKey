package com.xu.output.MessagingSystem.routing.outputStructures.block;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.output.AnalysisIssue;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import com.xu.analyzer.ruleCheckers.RuleList;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Legacy extends Structure{
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Legacy.class);

    public Legacy(EnvironmentInfo info) {super(info);}

    public OutputStructure deserialize(String filePath) throws ExceptionHandler {
        return null;
    }

    @Override
    public String handleOutput() throws ExceptionHandler {
        StringBuilder output = new StringBuilder();

        //reopening the console stream
        log.debug("Writing the Header");
        output.append(
                com.xu.output.MessagingSystem.routing.outputStructures.common.Legacy.marshallingHeader(
                        super.getType(), super.getSource().getSource()));

        //Only printing console output if it is set and there is output captured
        log.debug("Writing the Soot Errors");
        output.append(
                com.xu.output.MessagingSystem.routing.outputStructures.common.Legacy.marshallingSootErrors(
                        super.getSource().getSootErrors()));

        Map<Integer, List<AnalysisIssue>> groupedRules = new HashMap<>();
        log.debug("Grouping all of the rules according by number.");
        if (super.getCollection() != null)
            for (AnalysisIssue issue : super.getCollection()) {
                List<AnalysisIssue> tempList;
                if (groupedRules.containsKey(issue.getRuleId())) {
                    tempList = new ArrayList<>(groupedRules.get(issue.getRuleId()));
                    tempList.add(issue);
                } else {
                    tempList = Collections.singletonList(issue);
                }
                groupedRules.put(issue.getRuleId(), tempList);
            }

        //region Changing the order of the rules
        Set<Integer> ruleOrdering = new HashSet<>();
        log.debug("Ordering all of the rules based on the legacy output.");
        if (true) {
            Integer[] paperBasedOrdering = new Integer[] {3, 14, 6, 4, 12, 7, 11, 13, 9, 1, 10, 8, 5, 2};
            for (Integer rule : paperBasedOrdering)
                if (groupedRules.containsKey(rule)) ruleOrdering.add(rule);
        } else ruleOrdering = groupedRules.keySet();

        //endregion

        //region Broken Rule Cycle
        for (Integer ruleNumber : ruleOrdering) {
            log.debug("Working through the rule group " + ruleNumber);
            output.append("=======================================\n");
            output
                    .append("***Violated Rule ")
                    .append(RuleList.getRuleByRuleNumber(ruleNumber).getRuleId())
                    .append(": ")
                    .append(RuleList.getRuleByRuleNumber(ruleNumber).getDesc())
                    .append("\n");

            for (AnalysisIssue issue : groupedRules.get(ruleNumber)) {
                log.debug("Working through the broken rule " + issue.getInfo());
                output.append(
                        com.xu.output.MessagingSystem.routing.outputStructures.common.Legacy.marshalling(
                                issue, super.getType()));
            }

            output.append("=======================================\n");
        }
        //endregion

        //region Heuristics
    /*    if (super.getSource().getDisplayHeuristics()) {
            log.debug("Writing the heuristics");
            output.append(
                    frontEnd.MessagingSystem.routing.outputStructures.common.Legacy.marshalling(
                            super.getSource()));
        }*/
        //endregion

        //region Timing Section
        if (super.getSource().isShowTimes()) {
            log.debug("Writing the time measurements.");
            output.append(
                    com.xu.output.MessagingSystem.routing.outputStructures.common.Legacy.marshalling(
                            super.getSource().getAnalysisMilliSeconds()));
        }
        //endregion

        return output.toString();
    }
}
