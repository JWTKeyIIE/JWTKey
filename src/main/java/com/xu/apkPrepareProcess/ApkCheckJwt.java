package com.xu.apkPrepareProcess;

import com.alibaba.fastjson.JSONObject;
import com.xu.PrepareProcess.criteriagain.GainCriteriaRule;
import com.xu.PrepareProcess.criteriagain.JarCriteriaEntry;
import com.xu.PrepareProcess.criteriagain.preprocess.LibrariesProperty;
import com.xu.analyzer.differentEntry.EntryHandler;
import com.xu.environmentInit.ArgumentCheck;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ApkCheckJwt {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(ApkCheckJwt.class);
    public static void main(String[] args) throws ExceptionHandler, IOException {
        List<APKCheckResult> checkResults = new ArrayList<>();
        List<APKCheckResult> otherCheckResults = new ArrayList<>();
//        String apkPath = "/home/xu/Downloads/apk/google/art_and_design/";
//        String apkPath = "/home/xu/Downloads/apk/google/News_Magazines/";
//        String apkPath = "/home/xu/Downloads/apk/google/parenting/";
//        String apkPath = "/home/xu/Downloads/apk/google/personalization/";
//        String apkPath = "/home/xu/Downloads/apk/google/photography/";
//        String apkPath = "/home/xu/Downloads/apk/google/productivity";
//        String apkPath = "/home/xu/Downloads/apk/google/shopping/";
//        String apkPath= "/home/xu/Downloads/apk/google/social/";
//        String apkPath = "/home/xu/Downloads/apk/google/sports/";
//        String apkPath = "/home/xu/Downloads/apk/google/tools/";
//        String apkPath = "/home/xu/Downloads/apk/google/travel_and_local/";
//        String apkPath = "/home/xu/Downloads/apk/google/video_players/";
//        String apkPath = "/home/xu/Downloads/apk/google/weather/";
//        String apkPath = "/home/xu/Downloads/apk/google/1_new/";
        String apkPath = "/home/xu/xu/crawler/APKCrawler/data/fossdroid/";
/*      check google play app
        File file = new File(apkPath);
        File[] files = file.listFiles();
        for(File f: files){

            String fileName = f.getName();
            if(fileName.endsWith(".apk")){
                System.out.println(f.getAbsolutePath()+":::" + f.getName());
                APKCheckResult checkResult = new APKCheckResult(f.getName(),f.getParent());
                checkApkContainJwt(f.getAbsolutePath(),checkResult);
                checkResults.add(checkResult);
            }
        }
        System.out.println(checkResults.size());
        writeResultToJsonFile(apkPath+"CheckResult.json",checkResults);*/
        File file =  new File(apkPath);
        List<String> FileName = checkFolder(file);
        FileName.size();
        for(String fn: FileName){
            File tempFile = new File(fn);
            APKCheckResult checkResult = new APKCheckResult(tempFile.getName(),tempFile.getParent());
            if(checkApkContainJwt(fn,checkResult)){
                checkResults.add(checkResult);
                String libName = checkResult.getJwtLibName();
                if(checkResult.isUseJwtLib()){
                    switch (libName){
                        case "java-jwt":
                            copyFileUsingFileStreams(tempFile,"/home/xu/Desktop/foss/java-jwt/" + tempFile.getName());
                            break;
                        case"jjwt":
                            copyFileUsingFileStreams(tempFile,"/home/xu/Desktop/foss/jjwt/" + tempFile.getName());
                            break;
                        case"jose4j":
                            copyFileUsingFileStreams(tempFile,"/home/xu/Desktop/foss/jose4j/" + tempFile.getName());
                            break;
                        case"nimbus":
                            copyFileUsingFileStreams(tempFile,"/home/xu/Desktop/foss/nimbus/" + tempFile.getName());
                            break;
                        case"fusionauth":
                            copyFileUsingFileStreams(tempFile,"/home/xu/Desktop/foss/fusionauth/" + tempFile.getName());
                            break;
                        case"vertx":
                            copyFileUsingFileStreams(tempFile,"/home/xu/Desktop/foss/vertx/" + tempFile.getName());
                            break;
                        case "Java-Jwt-android":
                            copyFileUsingFileStreams(tempFile,"/home/xu/Desktop/foss/java-jwt-android/" + tempFile.getName());
                            break;
                        case"spring-jwt":
                            copyFileUsingFileStreams(tempFile,"/home/xu/Desktop/foss/spring-jwt/" + tempFile.getName());
                            break;
                        case "google-jwt":
                            copyFileUsingFileStreams(tempFile,"/home/xu/Desktop/foss/google-jwt/" + tempFile.getName());
                            break;
                    }
                }
                else {
                    otherCheckResults.add(checkResult);
                }

            }
        }
        writeResultToJsonFile("/home/xu/Desktop/foss/CheckResult.json",checkResults);
        writeResultToJsonFile("/home/xu/Desktop/foss/otherCheck.json", otherCheckResults);
    }



    public static List<String> checkFolder(File file){
        List<String> fileName = new ArrayList<>();
        File[] fs = file.listFiles();
        for(File f: fs){
            if(f.isDirectory()){
                fileName.addAll(checkFolder(f));
            }
            if(f.isFile()){
                if(f.getName().endsWith(".apk")){
                    fileName.add(f.getAbsolutePath());
                }
            }
        }
        return fileName;
    }

    public static boolean checkApkContainJwt(String apkPath,APKCheckResult apkCheckResult) throws ExceptionHandler {
        Boolean result = false;
        List<String> classNames = Utils.getClassNamesFromApkArchive(apkPath);
        List<String> otherJwtInfo = new ArrayList<>();
        log.info("Begin check Jwt lib");
        for(String className : classNames){
            if(className.contains("com.auth0.jwt")){
                log.info("java-jwt");
                apkCheckResult.setUseJwtLib(true);
                apkCheckResult.setJwtLibName("java-jwt");
                result = true;
                break;
            }
            if(className.contains("io.jsonwebtoken")){
                log.info("jjwt");
                apkCheckResult.setUseJwtLib(true);
                apkCheckResult.setJwtLibName("jjwt");
                result = true;
                break;
            }
            if(className.contains("org.jose4j")){
                log.info("jose4j");
                apkCheckResult.setUseJwtLib(true);
                apkCheckResult.setJwtLibName("jose4j");
                result = true;
                break;
            }
            if(className.contains("com.nimbusds.jose")){
                log.info("nimbus");
                apkCheckResult.setUseJwtLib(true);
                apkCheckResult.setJwtLibName("nimbus");
                result = true;
                break;
            }
            if(className.contains("io.fusionauth.jwt")){
                log.info("fusionauth");
                apkCheckResult.setUseJwtLib(true);
                apkCheckResult.setJwtLibName("fusionauth");
                result = true;
                break;
            }
            if(className.contains("io.vertx.ext.auth.jwt")){
                log.info("vertx");
                apkCheckResult.setUseJwtLib(true);
                apkCheckResult.setJwtLibName("vertx");
                result = true;
                break;
            }
            if(className.contains("com.auth0.android.jwt")){
                log.info("Java-Jwt-android");
                apkCheckResult.setUseJwtLib(true);
                apkCheckResult.setJwtLibName("Java-Jwt-android");
                result = true;
                break;
            }
            if(className.contains("org.springframework.security.oauth2.jwt")){
                log.info("spring-jwt");
                apkCheckResult.setUseJwtLib(true);
                apkCheckResult.setJwtLibName("spring-jwt");
                result = true;
                break;
            }
            if(className.contains("com.google.api.client.json.webtoken.JsonWebToken")){
                log.info("com.google.api.client.json.webtoken.JsonWebToken");
                apkCheckResult.setUseJwtLib(true);
                apkCheckResult.setJwtLibName("google-jwt");
                result = true;
                break;
            }
            else if(className.contains("jwt") ||className.toLowerCase().contains("jsonwebtoken")){
                log.info("other-jwt");
                log.info(className);
                otherJwtInfo.add(className);
                result = true;
            }
        }
        apkCheckResult.setOtherJwtInfo(otherJwtInfo);
        log.info("end check Jwt lib");
        return result;
    }
    public static void writeResultToJsonFile(String libPath, List<APKCheckResult> apkCheckResults) throws IOException {
        String resultJson = JSONObject.toJSONString(apkCheckResults);
        System.out.println(resultJson);
        FileWriter fileWriter = new FileWriter(new File(libPath));
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(resultJson);
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    /**
     * 将名称中包含空格的APK重命名
     * @param apkDirPath
     */
    public static void renameApk(String apkDirPath){
        File file = new File(apkDirPath);
        File[] files = file.listFiles();
        for(File f: files){
            String fileName = f.getName();
            if(fileName.endsWith(".apk")){
                if(fileName.contains(" ")){
                    f.renameTo(new File(f.getAbsolutePath().replace(" ","-")));
                }
                System.out.println(f.getAbsolutePath()+":::" + f.getName());
            }
        }
    }
    public static void renameAllApk(String apkDirPath){
        File file = new File(apkDirPath);
        File[] files = file.listFiles();
        for(int i = 1; i < files.length; i++){
            File f = files[i];
            String fileName = f.getName();
            String newFileName = fileName.split("_")[0];

        }
    }
    public static String run(EnvironmentInfo info) throws ExceptionHandler {
        EntryHandler handler = null;
        handler = new ApkCheckEntry();
        handler.Scan(info);
        return "123";
    }

    private static void copyFileUsingFileStreams(File source, String destDir)
            throws IOException {
        File dest = new File(destDir);
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
    }


}
