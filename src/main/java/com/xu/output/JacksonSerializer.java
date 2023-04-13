package com.xu.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import org.apache.logging.log4j.Logger;

public class JacksonSerializer {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(JacksonSerializer.class);

    /**
     * serialize.
     *
     * @param obj a {@link Object} object.
     * @param prettyPrint a {@link Boolean} object.
     * @param outputType a {@link
     *     JacksonType}
     *     object.
     * @return a {@link String} object.
     * @throws ExceptionHandler if any.
     */
    public static String serialize(Object obj, Boolean prettyPrint, JacksonType outputType)
            throws ExceptionHandler {
        ObjectMapper serializer = outputType.getOutputMapper();
        log.debug("Serializing output as " + outputType.name());
        serializer.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        if (prettyPrint) {
            log.debug("Writing with the \"pretty\" format");
            serializer.enable(SerializationFeature.INDENT_OUTPUT);
        }
        try {
            return serializer.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.fatal("Error marshalling output: " + e.getMessage());
            throw new ExceptionHandler("Error marshalling output", ExceptionId.MAR_VAR);
        }
    }

    /**
     * @author CryptoguardTeam Created on 4/6/19.
     * @since 03.04.08
     *     <p>{Description Here}
     */
    public enum JacksonType {
        //region Values
        XML(".xml", new XmlMapper()),
        JSON(".json", new ObjectMapper()),
        YAML(".yaml", new ObjectMapper(new YAMLFactory())),
        ;
        //endregion

        //region Attributes
        private String extension;
        private ObjectMapper outputMapper;
        //endregion

        //region constructor
        JacksonType(String ext, ObjectMapper mapper) {
            this.extension = ext;
            this.outputMapper = mapper;
        }

        //endregion

        //region Getter
        public String getExtension() {
            return extension;
        }

        public ObjectMapper getOutputMapper() {
            return outputMapper;
        }
        //endregion
    }
}
