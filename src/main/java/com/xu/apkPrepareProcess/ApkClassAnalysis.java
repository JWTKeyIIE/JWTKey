package com.xu.apkPrepareProcess;

import com.xu.Entry;
import com.xu.analyzer.differentEntry.*;
import com.xu.environmentInit.ArgumentCheck;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

public class ApkClassAnalysis {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(ApkClassAnalysis.class);
    public static void main(String[] args) throws ExceptionHandler, IOException {
        log.info("args length: " + args.length);
        ArrayList<String> strippedArgs = Utils.stripEmpty(args);

        log.info("check parameterCheck.");
        EnvironmentInfo environmentInfo = ArgumentCheck.parameterCheck(strippedArgs);
        log.debug("init info success");
        run(environmentInfo);
    }
    public static String run(EnvironmentInfo info) throws ExceptionHandler {
        EntryHandler handler = null;
        switch (info.getSourceType()){
            case APK:
                log.debug("Chosen APK Scanning");
                handler = new ApkAnalEntry();
        }
        log.debug("Initializing the scanning process");
        info.startScanning();

        log.info("Starting the scanning process");
        handler.Scan(info);
        log.info("Stopped the scanning process");

        info.stopScanning();
        log.info("Writing the output to the file: " + info.getFileOut());
        return info.getFileOut();
//        return "123";
    }

}
