package com.xu.PrepareProcess.criteriagain;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.analyzer.differentEntry.EntryHandler;
import com.xu.environmentInit.ArgumentCheck;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

public class GainCriteriaRule {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(GainCriteriaRule.class);
    public static void main(String[] args) throws ExceptionHandler, IOException {
/*        String libUrl = "/home/xu/xu/CertTest/target/CertTest-1.0-SNAPSHOT.jar";
        String dependencyUrl = "/home/xu/xu/CertTest/lib";
        String[] argsParam = new String[]{"-in", "jar","-s",libUrl,"-d",dependencyUrl,"-V"};*/
/*        String libUrl = "/home/xu/xu/JWTGuardTestLib/Cert_lib/spring-boot-oauth2-azuread/build/libs/spring-boot-oauth2-azuread-0.0.1-SNAPSHOT.jar";
        String dependencyUrl = "/home/xu/xu/JWTGuardTestLib/Cert_lib/spring-boot-oauth2-azuread/lib";
        String[] argsParam = new String[]{"-in", "jar","-s",libUrl,"-d",dependencyUrl,"-V"};*/
//        String libUrl = "/home/xu/xu/JWTGuard/FIDO2JWTVerify.jar";
//        String libUrl = "/home/xu/xu/JWTGuardTestLib/Cert_lib/web-utils/build/libs/web-utils-2.9.8.jar";
//        String libUrl = "/home/xu/xu/JWTGuard/TestJose4j-1.0-SNAPSHOT.jar";
        String libUrl = "/home/xu/xu/JWTGuard/video.jar";
        String[] argsParam = new String[]{"-in", "jar","-s",libUrl,"-V"};
        log.info("argsParam length: " + argsParam.length);

        ArrayList<String> strippedArgsParam = Utils.stripEmpty(argsParam);

        EnvironmentInfo environmentInfo = ArgumentCheck.parameterCheck(strippedArgsParam);
        run(environmentInfo);




    }
    public static String run(EnvironmentInfo info) throws ExceptionHandler {
        EntryHandler handler = null;
        handler = new JarCriteriaEntry();
        handler.Scan(info);
        return "123";
    }

}
