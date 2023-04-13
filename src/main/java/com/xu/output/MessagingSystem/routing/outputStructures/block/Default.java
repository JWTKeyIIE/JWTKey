/*package com.xu.output.MessagingSystem.routing.outputStructures.block;

import ExceptionHandler;
import com.xu.environmentInit.EnvironmentInfo;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Default extends Structure{
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Default.class);

    public Default(EnvironmentInfo info) {super(info);}
    public Default(String filePath) throws ExceptionHandler {
        Report struct = Report.deserialize(new File(filePath));

        EnvironmentInfo info = mapper(struct);
        super.setSource(info);
        super.setOutfile(new File(info.getFileOut()));
        super.setType(mapper(struct.getTarget().getType()));

        for (frontEnd.MessagingSystem.routing.structure.Default.Issue issue : struct.getIssues())
            super.addIssueToCollection(mapper(issue));
    }

    @Override
    public String handleOutput() throws ExceptionHandler {
        return null;
    }
}*/
