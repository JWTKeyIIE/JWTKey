package com.xu.output.MessagingSystem.routing.outputStructures.common;

import com.xu.environmentInit.enumClass.SourceType;
import com.xu.output.AnalysisIssue;
import com.xu.output.AnalysisLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Legacy {
    public static String marshallingHeader(SourceType sourceType, List<String> sources) {
        StringBuilder out = new StringBuilder();

        out.append("Analyzing ").append(sourceType.getName()).append(": ");
        for (int sourceKtr = 0; sourceKtr < sources.size(); sourceKtr++) {
            out.append(sources.get(sourceKtr));

            if (sourceKtr != sources.size() - 1) out.append(",");
        }
        out.append("\n");
        out.append("=======================================\n" + "         *** JWT misuse****          \n" + "=======================================\n");

        return out.toString();
    }

    public static String marshallingSootErrors(String soot) {
        if (StringUtils.isBlank(soot)) return "";

        return "=======================================\n"
                + "Internal Warnings: \n"
                + soot
                + "\n"
                + "=======================================\n";
    }

    public static String marshalling(AnalysisIssue issue, SourceType sourceType) {
        StringBuilder out = new StringBuilder();

        if (StringUtils.isNotBlank(issue.getClassName())) {
            out.append("***");
            if (!issue.getInfo().equals("UNKNOWN")) out.append(issue.getInfo());
            else out.append(issue.getRule().getDesc());
        } else {
            out.append("***Found: ");
            out.append("[\"").append(issue.getInfo()).append("\"] ");
        }

        //region Location Setting
        String lines = null;
        if (issue.getLocations().size() > 0) {

            List<AnalysisLocation> issueLocations = new ArrayList<>();
            for (AnalysisLocation loc : issue.getLocations())
                if (loc.getMethodNumber() == issue.getMethods().size() - 1) issueLocations.add(loc);

            if (!issueLocations.isEmpty() && !issueLocations.toString().contains("-1"))
                lines = ":" + issueLocations.toString().replace("[", "").replace("]", "");
        }

        out.append(" in ").append(issue.getClassName());

        if (sourceType.equals(SourceType.DIR) || sourceType.equals(SourceType.JAVAFILES))
            out.append(".java");
        else if (sourceType.equals(SourceType.CLASSFILES)) out.append(".class");

        out.append("::").append(issue.getMethods().pop());

        if (lines != null) out.append(lines);

        out.append(".");
        //endregion

        //endregion
        out.append("\n");

        return out.toString();
    }

    public static String marshalling(Long analysisTime) {

        return "=======================================\n"
                + "Analysis Timing (ms): "
                + analysisTime
                + ".\n"
                + "=======================================\n";
    }

  /*  public static String marshalling(EnvironmentInfo info) {

        StringBuilder out = new StringBuilder();

        out.append("Total Heuristics: ")
                .append(info.getHeuristics().getNumberOfHeuristics())
                .append("\n");
        out.append("Total Orthogonal: ")
                .append(info.getHeuristics().getNumberOfOrthogonal())
                .append("\n");
        out.append("Total Constants: ")
                .append(info.getHeuristics().getNumberOfConstantsToCheck())
                .append("\n");
        out.append("Total Slices: ").append(info.getHeuristics().getNumberOfSlices()).append("\n");
        out.append("Average Length: ")
                .append(info.getSLICE_AVERAGE_3SigFig())
                .append("\n")
                .append("\n");

        for (String depth : info.getHeuristics().getDepthCount()) out.append(depth).append("\n");

        return out.toString();
    }*/
}
