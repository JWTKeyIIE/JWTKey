package com.xu.analyzer.differentEntry;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.EnvironmentInfo;

public interface EntryHandler {
    void Scan(EnvironmentInfo info) throws ExceptionHandler;
}
