package com.xu.output.MessagingSystem.routing.outputStructures.stream;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;



public class Legacy extends Structure{

    public Legacy(EnvironmentInfo info) throws ExceptionHandler{
        super(info);
    }

    public OutputStructure deserialize(String filePath) throws ExceptionHandler {
        return null;
    }

    @Override
    public void writeHeader() throws ExceptionHandler {
        this.writeln(com.xu.output.MessagingSystem.routing.outputStructures.common.Legacy.marshallingHeader(super.getType(), super.getSource().getSource()));
    }

    @Override
    public void writeFooter() throws ExceptionHandler {
        this.writeln(com.xu.output.MessagingSystem.routing.outputStructures.common.Legacy.marshallingSootErrors(super.getSource().getSootErrors()));

        //region Heuristics
       /* if (super.getSource().getDisplayHeuristics()) {
            this.writeln(
                    com.xu.output.MessagingSystem.routing.outputStructures.common.Legacy.marshalling(super.getSource()));
        }*/
        //endregion

        if (super.getSource().isShowTimes()) {
            this.writeln(
                    com.xu.output.MessagingSystem.routing.outputStructures.common.Legacy.marshalling(
                            super.getSource().getAnalysisMilliSeconds()));
        }
    }
}
