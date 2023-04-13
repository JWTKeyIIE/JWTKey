package com.xu.apkPrepareProcess;

import com.xu.analyzer.differentEntry.EntryHandler;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.environmentInit.Exception.ExceptionHandler;
import org.apache.logging.log4j.Logger;

public class ApkAnalEntry implements EntryHandler {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(ApkAnalEntry.class);

    @Override
    public void Scan(EnvironmentInfo info) throws ExceptionHandler {
        log.debug("Starting scanner looper");

    }
}
