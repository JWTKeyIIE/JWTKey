package com.xu.apkPrepareProcess;

import com.xu.analyzer.differentEntry.EntryHandler;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Locale;

public class ApkCheckEntry implements EntryHandler {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(ApkCheckJwt.class);
    @Override
    public void Scan(EnvironmentInfo info) throws ExceptionHandler {
        String projectApkPath = info.getSource().get(0);
        List<String> classNames = Utils.getClassNamesFromApkArchive(projectApkPath);
        log.info("Begin check Jwt lib");
        for(String className : classNames){
            if(className.contains("com.auth0.jwt")){
                log.info("Java-Jwt");
            }
            if(className.contains("io.jsonwebtoken")){
                log.info("jjwt");
            }
            if(className.contains("org.jose4j")){
                log.info("jose4j");
            }
            if(className.contains("com.nimbusds.jose")){
                log.info("nimbus");
            }
            if(className.contains("io.fusionauth.jwt")){
                log.info("fusionauth");
            }
            if(className.contains("io.vertx.ext.auth.jwt")){
                log.info("vertx");
            }
            if(className.contains("com.auth0.android.jwt")){
                log.info("Java-Jwt-android");
            }
            if(className.contains("org.springframework.security.oauth2.jwt")){
                log.info("spring-jwt");
            }
            else if(className.contains("jwt") ||className.toLowerCase().contains("jsonwebtoken")){
                log.info("other-jwt");
                log.info(className);
            }
        }
        log.info("end check Jwt lib");
    }
}
