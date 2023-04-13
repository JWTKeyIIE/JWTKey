package com.xu.output.MessagingSystem.routing.outputStructures.stream;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;

public abstract class Structure extends OutputStructure {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Structure.class);

    private FileOutputStream streamOut;

    public Structure(EnvironmentInfo info) throws ExceptionHandler {
        super(info);
        try {
            this.streamOut = new FileOutputStream(super.getOutfile());
        } catch (Exception e) {
            log.fatal("Error creating the output stream with " + info.getFileOutName());
            throw new ExceptionHandler(
                    "Error creating the output stream with " + info.getFileOutName(), ExceptionId.FILE_CON);
        }
    }

    public Structure() {}

    @Override
    public void startAnalyzing() throws ExceptionHandler {
        writeHeader();
    }

    @Override
    public void stopAnalyzing() throws ExceptionHandler {
        writeFooter();
        close();
    }

    public abstract void writeHeader() throws ExceptionHandler;

    public abstract void writeFooter() throws ExceptionHandler;

    public void writeln(String output) throws ExceptionHandler {
        this.write(output + "\n");
    }

    public void write(String output) throws ExceptionHandler {
        //output = StringUtils.trimToNull(output);
        if (output != null)
            try {
                this.streamOut.write(output.getBytes(super.getChars()));
            } catch (Exception e) {
                log.fatal("Error writing the output " + output);
                throw new ExceptionHandler("Error writing the output " + output, ExceptionId.FILE_O);
            }
    }

    public void close() throws ExceptionHandler {
        try {
            this.streamOut.close();
        } catch (Exception e) {
            log.fatal("Error closing the stream.");
            throw new ExceptionHandler("Error closing the stream.", ExceptionId.FILE_CUT);
        }
    }
}
