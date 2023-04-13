package com.xu;

import com.xu.analyzer.differentEntry.*;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.ArgumentCheck;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

/*程序入口*/
public class Entry {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Entry.class);
    public static void main(String[] args) throws ExceptionHandler, IOException {
        //解析参数

        log.info("args length: " + args.length);
        ArrayList<String> strippedArgs = Utils.stripEmpty(args);

        log.info("check parameterCheck.");
        EnvironmentInfo environmentInfo = ArgumentCheck.parameterCheck(strippedArgs);
        log.debug("init info success");
        run(environmentInfo);





/*        List source = new ArrayList();
//        source.add("/home/xu/xu/cryptoguard_learn/samples/SimpleJavaJar3");
        source.add("samples/Crypto_Example");
        String javaHome = "/home/xu/java/jdk1.7.0_80";
        List dependence = new ArrayList<>();
//        dependence.add("samples/SimpleJavaJar3/build/dependencies");
        //环境信息的检查

        //检查明文存储对称密钥的问题
        initSoot(javaHome,source,dependence);*/

    }
    public static String run(EnvironmentInfo info) throws ExceptionHandler {
        EntryHandler handler = null;
        switch (info.getSourceType()){
            case JAR:
                log.debug("Chosen JAR Scanning");
                handler = new JarEntry();
                break;
            case DIR:
                log.debug("Chosen DIR Scanning");
                handler = new SourceEntry();
                break;
            case WAR:
                log.debug("Chosen War Scanning");
                handler = new WarEntry();
            case APK:
                log.debug("Chosen APK Scanning");
                handler = new ApkEntry();
        }
        log.debug("Initializing the scanning process");
        info.startScanning();

        log.info("Starting the scanning process");
        handler.Scan(info);
        log.info("Stopped the scanning process");

        info.stopScanning();
        log.info("Writing the output to the file: " + info.getFileOut());
        log.info("Runtime: "+ info.getAnalysisMilliSeconds());
        return info.getFileOut();
//        return "123";
    }
}
