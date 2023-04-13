package com.xu.output.MessagingSystem.routing.outputStructures.block;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.output.AnalysisIssue;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

public abstract class Structure extends OutputStructure {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Structure.class);

    public Structure(EnvironmentInfo info) {
        super(info);
    }

    public Structure() {}


    @Override
    public void startAnalyzing() throws ExceptionHandler {

    }

    @Override
    public void stopAnalyzing() throws ExceptionHandler {
        WriteIntoFile(StringUtils.stripToNull(this.handleOutput()));
    }

    @Override
    public void addIssue(AnalysisIssue issue) throws ExceptionHandler {
        super.addIssueToCollection(issue);
    }

    public abstract String handleOutput() throws ExceptionHandler;

    public void WriteIntoFile(String in) throws ExceptionHandler {
        try {
            Files.write(this.getOutfile().toPath(), in.getBytes(super.getChars()));
        } catch (IOException e) {
            log.fatal("Error: " + e.getMessage());
            throw new ExceptionHandler(
                    "Error writing to file: " + this.getSource().getFileOutName(), ExceptionId.FILE_O);
        }
    }

}
