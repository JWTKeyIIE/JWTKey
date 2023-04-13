package com.xu.environmentInit;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import com.xu.environmentInit.enumClass.Listing;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.output.AnalysisIssue;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import com.xu.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import soot.G;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Pattern;

//用于定义程序需要的环境信息
public class EnvironmentInfo {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(EnvironmentInfo.class);

    //分析程序以及运行设备相关信息
    private final java.util.Properties Properties = new Properties();
    private final String PropertiesFile = "gradle.properties";
    private String ToolFramework;
    private String ToolFrameworkVersion;
    private String platformName = Utils.getPlatform();
    private Long analysisMilliSeconds;
    private Long startAnalyisisTime;

    //需要输入的被分析文件路径以及相关信息
    private SourceType sourceType;
    private final List<String> Source;
    private final List<String> sourcePaths;
    private List<String> dependencies;
    private Listing messagingType;

    private String rawCommand;

    //Dir类型所需信息
    private Boolean isGradle;
    private String targetProjectName;
    private String targetProjectVersion;

    //非必须的输入信息
    private String packageName = "UNKNOWN";
    private String packageRootDir = "UNKNOWN";
    private String buildRootDir = "UNKNOWN";
    String javaHome;
    String androidHome;
    String main;
    private String fileOut;
    private Boolean killJVM = true;

    //输出相关信息
    private Boolean prettyPrint = false;
    private boolean showTimes = false;
    private Boolean streaming = false;
    private OutputStructure output;
    private Function<AnalysisIssue, String> errorAddition;
    private Function<HashMap<Integer, Integer>, String> bugSummaryHandler;



    private Boolean displayHeuristics = false;


    private ByteArrayOutputStream sootErrors = new ByteArrayOutputStream();

    //jwt相关参数
    private String jwtLibName;
    private String jwtInfoPath;


    public EnvironmentInfo(
            @Nonnull List<String> source,
            @Nonnull SourceType sourceType,
            Listing messageType,
            List<String> dependencies,
            List<String> sourcePaths,
            String sourcePkg) throws ExceptionHandler {
        log.info("Begin init EnvironmentInformation");

        //设置版本信息
        String tempToolFrameworkVersion;
        String tempToolFramework;

        try {
            Properties.load(new FileInputStream(PropertiesFile));
            tempToolFrameworkVersion = Properties.getProperty("versionNumber");
            tempToolFramework = Properties.getProperty("projectName");
        } catch (IOException e) {
            tempToolFrameworkVersion = "Property Not Found";
            tempToolFramework = "Property Not Found";
        }

        ToolFrameworkVersion = tempToolFrameworkVersion;
        ToolFramework = tempToolFramework;


        G.v().out = new PrintStream(this.sootErrors);
        this.Source = source;
        this.sourceType = sourceType;
        this.sourcePaths = sourcePaths;
        if(dependencies !=null){
            this.dependencies = dependencies;
        }

        this.messagingType = messageType;
        String[] pkgs = sourcePkg.split(Pattern.quote(System.getProperty("file.separator")));
//        String[] pkgs = sourcePkg.split(System.getProperty("file.separator"));
        this.packageName = pkgs[pkgs.length - 1].split("\\.")[0];

        this.setPackageRootDir();
        this.setBuildRootDir();

    }

    /**
     * 验证EnvironmentInfo的基本信息，配置java路径
     * @throws ExceptionHandler
     */
    public void verifyBaseSetting() throws ExceptionHandler {
        switch (this.sourceType){
            case DIR:
            case JAVAFILES:
                if(StringUtils.isEmpty(getJavaHome())){
                    log.fatal("Please set JAVA7_HOME or specify via the arguments.");
                    throw new ExceptionHandler(
                            "Please set JAVA7_HOME or specify via the arguments.", ExceptionId.ENV_VAR);
                }
                break;
            case APK:
                if (StringUtils.isEmpty(getAndroidHome())) {
                    log.fatal("Please set ANDROID_HOME or specify via the arguments.");
                    throw new ExceptionHandler(
                            "Please set ANDROID_HOME or specify via the arguments.", ExceptionId.ENV_VAR);
                }
            case JAR:
            case WAR:
            case CLASSFILES:
                if (StringUtils.isEmpty(getJavaHome())) {
                    log.fatal("Please set JAVA_HOME or specify via the arguments.");
                    throw new ExceptionHandler(
                            "Please set JAVA_HOME or specify via the arguments.", ExceptionId.ENV_VAR);
                }
                break;
        }
    }
    public String getAndroidHome() {
        if (StringUtils.isEmpty(this.androidHome))
             this.androidHome = System.getenv("ANDROID_HOME").replaceAll("//", "/");
//            this.androidHome = "/home/xu/Android/android-sdk-linux";

        return this.androidHome;
    }
    public void startScanning() throws ExceptionHandler {
        this.getOutput().startAnalyzing();
        this.startAnalysis();
    }
    public void startAnalysis() {
        this.startAnalyisisTime = System.currentTimeMillis();
    }

    public void stopScanning() throws ExceptionHandler {
        this.stopAnalysis();
 /*       this.setHuristicsInfo();

        if (this.getHeuristicsHandler() != null)
            log.info(this.getHeuristicsHandler().apply(this.getHeuristics()));*/

        this.getOutput().stopAnalyzing();
    }
    public void stopAnalysis() {
        this.analysisMilliSeconds = System.currentTimeMillis() - this.startAnalyisisTime;
    }
/*    public void setHuristicsInfo() {
        this.heuristics.setNumberOfOrthogonal(Utils.NUM_ORTHOGONAL);
        this.heuristics.setNumberOfConstantsToCheck(Utils.NUM_CONSTS_TO_CHECK);
        this.heuristics.setNumberOfSlices(Utils.NUM_SLICES);
        this.heuristics.setNumberOfHeuristics(Utils.NUM_HEURISTIC);
        this.heuristics.setSliceAverage(Utils.calculateAverage());
        this.heuristics.setDepthCount(Utils.createDepthCountList());
    }*/

    public String getSootErrors() {
        return StringUtils.trimToEmpty(sootErrors.toString());
    }

    public void setSootErrors(ByteArrayOutputStream sootErrors) {
        this.sootErrors = sootErrors;
    }

    public Long getAnalysisMilliSeconds() {
        return analysisMilliSeconds;
    }

    public void setAnalysisMilliSeconds(Long analysisMilliSeconds) {
        this.analysisMilliSeconds = analysisMilliSeconds;
    }

    public Long getStartAnalyisisTime() {
        return startAnalyisisTime;
    }

    public void setStartAnalyisisTime(Long startAnalyisisTime) {
        this.startAnalyisisTime = startAnalyisisTime;
    }

    public OutputStructure getOutput() throws ExceptionHandler {
        if (this.output == null)
            this.output = this.messagingType.getTypeOfMessagingOutput(this.streaming, this);

        return this.output;
    }
    public String getFileOutName() {
        String[] split = this.fileOut.split(System.getProperty("file.separator"));

        return split[split.length - 1];
    }

    public void setOutput(OutputStructure output) {
        this.output = output;
    }

    public String getBuildRootDir() {
        return buildRootDir;
    }

    public void setBuildRootDir() throws ExceptionHandler {
        log.info("Building the Root Directory");
        switch (this.getSourceType()) {
            case APK:
            case DIR:
            case WAR:
            case JAR:
                try {
                    this.buildRootDir = new File(sourcePaths.get(0)).getCanonicalPath();
                } catch (IOException e) {
                    log.fatal("Error reading file: " + buildRootDir);
                    throw new ExceptionHandler("Error reading file: " + buildRootDir, ExceptionId.FILE_I);
                }
                break;
            case JAVAFILES:
            case CLASSFILES:
                this.buildRootDir = Utils.retrievePackageFromJavaFiles(sourcePaths);
                break;
        }
    }

    public String getPackageRootDir() {
        return packageRootDir;
    }

    public void setPackageRootDir() throws ExceptionHandler {
        log.info("Build the package root dir based on type");
        switch (this.getSourceType()){
            case JAR:
            case WAR:
            case APK:
            case DIR:
                String[] split = this.getSource().get(0).split(Pattern.quote(Utils.fileSep));
//                String[] split = this.getSource().get(0).split(Utils.fileSep);
                this.packageRootDir = split[split.length - 1] + Utils.fileSep;
                break;
            case JAVAFILES:
            case CLASSFILES:
                this.packageRootDir = Utils.getRelativeFilePath(packageRootDir);
                break;
        }
    }
    public Listing getMessagingType() {
        return messagingType;
    }

    public void setMessagingType(Listing messagingType) {
        this.messagingType = messagingType;
    }
    public String getJavaHome() {
        if (StringUtils.isEmpty(this.javaHome))
            switch (this.sourceType) {
                case CLASSFILES:
                case WAR:
                case JAR:
                case APK:
                    this.javaHome = System.getenv("JAVA_HOME").replaceAll("//", "/");
                    break;
                case JAVAFILES:
                case DIR:
          this.javaHome = System.getenv("JAVA7_HOME").replaceAll("//", "/");
//                    this.javaHome = "/home/xu/java/jdk1.7.0_80";
//                    this.javaHome = "/home/xu/java/jdk1.8.0_181";
//          this.javaHome = System.getenv("JAVA_HOME").replaceAll("//", "/");
                    break;
            }

        return this.javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public List<String> getSource() {
        return this.Source;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getMain() {
        return main;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getFileOut() {
        return fileOut;
    }

    public void setFileOut(String fileOut) {
        this.fileOut = fileOut;
    }

    public Boolean getPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(Boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public boolean isShowTimes() {
        return showTimes;
    }

    public void setShowTimes(boolean showTimes) {
        this.showTimes = showTimes;
    }

    public Boolean getStreaming() {
        return streaming;
    }

    public void setStreaming(Boolean streaming) {
        this.streaming = streaming;
    }
    public Boolean getDisplayHeuristics() {
        return displayHeuristics;
    }

    public void setDisplayHeuristics(Boolean displayHeuristics) {
        this.displayHeuristics = displayHeuristics;
    }

    public Boolean getKillJVM() {
        return killJVM;
    }

    public void setKillJVM(Boolean killJVM) {
        this.killJVM = killJVM;
    }

    public String getRawCommand() {
        return rawCommand;
    }

    public void setRawCommand(String rawCommand) {
        this.rawCommand = rawCommand;
    }

    public Boolean getGradle() {
        return isGradle;
    }

    public void setGradle(Boolean gradle) {
        isGradle = gradle;
    }

    public String getTargetProjectName() {
        return targetProjectName;
    }

    public void setTargetProjectName(String targetProjectName) {
        this.targetProjectName = targetProjectName;
    }

    public String getTargetProjectVersion() {
        return targetProjectVersion;
    }

    public void setTargetProjectVersion(String targetProjectVersion) {
        this.targetProjectVersion = targetProjectVersion;
    }

    public List<String> getDependencies() {
        if (dependencies == null) dependencies = new ArrayList<>();
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getSourcePaths() {
        return sourcePaths;
    }

    public String getJwtLibName() {
        return jwtLibName;
    }

    public void setJwtLibName(String jwtLibName) {
        this.jwtLibName = jwtLibName;
    }

    public String getJwtInfoPath() {
        return jwtInfoPath;
    }

    public void setJwtInfoPath(String jwtInfoPath) {
        this.jwtInfoPath = jwtInfoPath;
    }

    public Function<AnalysisIssue, String> getErrorAddition() {
        return errorAddition;
    }

    public void setErrorAddition(Function<AnalysisIssue, String> errorAddition) {
        this.errorAddition = errorAddition;
    }

    public Function<HashMap<Integer, Integer>, String> getBugSummaryHandler() {
        return bugSummaryHandler;
    }

    public void setBugSummaryHandler(Function<HashMap<Integer, Integer>, String> bugSummaryHandler) {
        this.bugSummaryHandler = bugSummaryHandler;
    }
}
