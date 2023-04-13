package com.xu.environmentInit.enumClass;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.output.JacksonSerializer;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import com.xu.output.MessagingSystem.routing.outputStructures.stream.Legacy;
import com.xu.output.MessagingSystem.routing.outputStructures.stream.Structure;
import org.apache.logging.log4j.Logger;

import java.util.List;

public enum Listing {
    Legacy("Legacy", "L", ".txt", true, null),
    ScarfXML("ScarfXML", "SX", null, true, JacksonSerializer.JacksonType.XML),
    Default("Default", "D", null, true, JacksonSerializer.JacksonType.JSON),
    YAMLGeneric("Default", "Y", null, true, JacksonSerializer.JacksonType.YAML),
    XMLGeneric("Default", "X", null, true, JacksonSerializer.JacksonType.XML),
    CSVDefault("CSVDefault", "CSV", ".csv", true, null);
    //endregion
    //region Attributes
    private final String blockPath = "com.xu.output.MessagingSystem.routing.outputStructures.block.";
    private final String inputPath = "frontEnd.MessagingSystem.routing.inputStructures";
    private final String streamPath = "com.xu.output.MessagingSystem.routing.outputStructures.stream.";
    private final String typeSpecificArgPath = "frontEnd.Interface.formatArgument.";
    private String type;
    private String flag;
    private String outputFileExt;
    private Boolean streamEnabled;
    private JacksonSerializer.JacksonType jacksonType;
    private Logger log;
    //endregion

    //region Constructor

    /**
     * The inherint constructor of all the enum value types listed here
     *
     * @param Type - the string value of the type of
     * @param Flag - the flag used to identify the specific messaging type
     */
    Listing(
            String Type,
            String Flag,
            String outputFileExt,
            Boolean streamed,
            JacksonSerializer.JacksonType jacksonType) {
        this.type = Type;
        this.flag = Flag;
        this.outputFileExt = outputFileExt;
        this.streamEnabled = streamed;
        this.jacksonType = jacksonType;
        this.log = org.apache.logging.log4j.LogManager.getLogger(EnvironmentInfo.class);
    }
    //endregion

    //region Overridden Methods

    /**
     * getInputFullHelp.
     *
     * @return a {@link String} object.
     */
    public static String getInputFullHelp() {
        StringBuilder out = new StringBuilder();

        for (Listing listingType : Listing.values()) {
      /*
      try {
          out.append(listingType.retrieveSpecificArgHandler().helpInfo()).append("\n");
      } catch (ExceptionHandler e) {}
      */
        }

        return out.toString();
    }
    //endregion

    //region Getter

    /**
     * The dynamic loader for the Listing Type based on the flag
     *
     * @param flag {@link String} - The input type looking for the flag type
     * @return {@link Listing} - The messaging Type retrieved by
     *     flag, if not found the default will be used
     */
    public static Listing retrieveListingType(String flag) {
        if (flag != null) for (Listing type : Listing.values()) if (type.flag.equals(flag)) return type;

        return Listing.Default;
    }

    /**
     * getShortHelp.
     *
     * @return a {@link String} object.
     */
    public static String getShortHelp() {
        StringBuilder helpString = new StringBuilder("{");

        for (Listing in : Listing.values())
            helpString.append(in.getType()).append(": ").append(in.getFlag()).append(", ");

        helpString.append("}");

        return helpString.toString();
    }

    /**
     * Getter for flag
     *
     * <p>getFlag()
     *
     * @return a {@link String} object.
     */
    public String getFlag() {
        return flag;
    }

    /**
     * retrieveArgs.
     *
     * @return a {@link List} object.
     */
    public List<argsIdentifier> retrieveArgs() {
        return argsIdentifier.lookup(this);
    }
    //endregion

    //region Helpers Based on the enum type

    //region Output Helpers

    /**
     * Getter for type
     *
     * <p>getType()
     *
     * @return {@link String} - The type.
     */
    public String getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "{ \"type\": \"" + this.type + "\", \"flag\": \"" + this.flag + "\"}";
    }

    //endregion

    //region InputHelpers

    /**
     * getTypeOfMessagingOutput.
     *
     * @param stream a boolean.
     * @param info a {@link EnvironmentInfo} object.
     * @return a {@link OutputStructure} object.
     * @throws ExceptionHandler if any.
     */
    public OutputStructure getTypeOfMessagingOutput(boolean stream, EnvironmentInfo info)
            throws ExceptionHandler {

        if (stream) {
            if (!this.streamEnabled) {
                log.info("Streaming is not supported for the format: " + this.getType());
                log.info("Defaulting back to block based output.");
                return getTypeOfMessagingOutput(false, info);
            } else {
                try {
                    return (Structure)
                            Class.forName(this.streamPath + this.type)
                                    .getConstructor(EnvironmentInfo.class)
                                    .newInstance(info);
                } catch (Exception e) {
                    return new Legacy(info);
                }
            }
        } else //non-streamed
        {
            try {
                return (com.xu.output.MessagingSystem.routing.outputStructures.block.Structure)
                        Class.forName(this.blockPath + this.type)
                                .getConstructor(EnvironmentInfo.class)
                                .newInstance(info);
            } catch (Exception e) {
                return new com.xu.output.MessagingSystem.routing.outputStructures.block.Legacy(info);
            }
        }
    }

    /**
     * unmarshall.
     *
     * @param stream a boolean.
     * @param filePath a {@link String} object.
     * @return a {@link OutputStructure} object.
     * @throws ExceptionHandler if any.
     */
  /*  public OutputStructure unmarshall(boolean stream, String filePath) throws ExceptionHandler {
        if (stream) {
            if (!this.streamEnabled) {
                log.info("Streaming is not supported for the format: " + this.getType());
                log.info("Defaulting to block based output.");
                return unmarshall(false, filePath);
            } else {
                try {
                    return (frontEnd.MessagingSystem.routing.outputStructures.stream.Structure)
                            Class.forName(this.streamPath + this.type)
                                    .getConstructor(String.class)
                                    .newInstance(filePath);
                } catch (Exception e) {
                    log.fatal(
                            "Issue dynamically calling the stream TypeSpecificArg with the filepath: "
                                    + filePath);
                    throw new ExceptionHandler(
                            ExceptionId.ARG_VALID,
                            "Issue dynamically calling the TypeSpecificArg with the filepath: " + filePath);
                }
            }
        } else //non-streamed
        {
            try {
                return (Structure)
                        Class.forName(this.blockPath + this.type)
                                .getConstructor(String.class)
                                .newInstance(filePath);
            } catch (Exception e) {
                log.fatal(
                        "Issue dynamically calling the blocked TypeSpecificArg with the filepath: " + filePath);
                throw new ExceptionHandler(
                        ExceptionId.ARG_VALID,
                        "Issue dynamically calling the TypeSpecificArg with the filepath: " + filePath);
            }
        }
    }

    *//**
     * retrieveSpecificArgHandler.
     *
     * @return a {@link TypeSpecificArg} object.
     * @throws ExceptionHandler if any.
     */
/*    public TypeSpecificArg retrieveSpecificArgHandler() throws ExceptionHandler {
        try {
            return (TypeSpecificArg)
                    Class.forName(this.typeSpecificArgPath + this.type).getConstructor().newInstance();
        } catch (Exception e) {
            log.warn(
                    "Issue dynamically calling the specific argument validator: "
                            + this.typeSpecificArgPath
                            + this.type);
            throw new ExceptionHandler(
                    ExceptionId.ENV_VAR,
                    "Issue dynamically calling the specific argument validator: "
                            + this.typeSpecificArgPath
                            + this.type);
        }
    }*/
    /**
     * Getter for the field <code>outputFileExt</code>.
     *
     * @return a {@link String} object.
     */
    public String getOutputFileExt() {

        if (outputFileExt == null) return this.jacksonType.getExtension();
        else return outputFileExt;
    }

    /**
     * Getter for the field <code>jacksonType</code>.
     *
     * @return a {@link
     *     JacksonSerializer.JacksonType}
     *     object.
     */
    public JacksonSerializer.JacksonType getJacksonType() {
        return jacksonType;
    }
    //endregion

    //endregion
}
