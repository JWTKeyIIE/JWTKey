package com.xu.apkPrepareProcess;

import java.util.ArrayList;
import java.util.List;

public class APKCheckResult {
    String APKName;
    String ApkPath;
    //是否使用JWT库
    boolean useJwtLib;
    //使用的JWT库名
    String JwtLibName;
    //其他搜索到的与JWT相关的结果
    List<String> OtherJwtInfo;

    public APKCheckResult(String APKName, String apkPath) {
        this.APKName = APKName;
        ApkPath = apkPath;
        this.useJwtLib = false;
    }

    public boolean isUseJwtLib() {
        return useJwtLib;
    }

    public void setUseJwtLib(boolean useJwtLib) {
        this.useJwtLib = useJwtLib;
    }

    public String getJwtLibName() {
        return JwtLibName;
    }

    public void setJwtLibName(String jwtLibName) {
        JwtLibName = jwtLibName;
    }

    public List<String> getOtherJwtInfo() {
        return OtherJwtInfo;
    }

    public void setOtherJwtInfo(List<String> otherJwtInfo) {
        OtherJwtInfo = otherJwtInfo;
    }

    public String getAPKName() {
        return APKName;
    }

    public String getApkPath() {
        return ApkPath;
    }
}
