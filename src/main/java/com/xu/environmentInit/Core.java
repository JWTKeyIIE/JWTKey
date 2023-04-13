package com.xu.environmentInit;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import com.xu.environmentInit.enumClass.Listing;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.environmentInit.enumClass.Version;
import com.xu.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.xu.utils.Utils.retrieveFullyQualifiedName;

public class Core {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Core.class);


    public static EnvironmentInfo parameterCheck(
            List<String> sourceFiles,
            List<String> dependencies,
            SourceType eType,
            Listing oType,
            String fileOutPath,
            Boolean overWriteFileOut,
            String mainFile,
            Boolean timeStamp,
            String java,
            List<String> extraArguments,
            String jwtLibName) throws ExceptionHandler, IOException {
        //get the java version
        Version currentVersion = Version.getRunningVersion();
        //要使用java 1.8
        if (StringUtils.isBlank(java) && !currentVersion.supported()) {
            log.fatal("JRE Version: " + currentVersion + " is not compatible");
            throw new ExceptionHandler(
                    "JRE Version: "
                            + currentVersion
                            + " is not compatible, please use JRE Version: "
                            + Version.EIGHT,
                    ExceptionId.GEN_VALID);
        }

        log.info("Retrieve the source files");
/*        ArrayList<String> vSources = new ArrayList<>();*/
/*        if(sourceFiles.size() == 1 && sourceFiles.get(0).equals("xargs")){
            vSources = Utils.retrievingThroughXArgs(eType,false,true);
        }else {
            vSources = Utils.retrieveFilePathTypes(new ArrayList<>(sourceFiles),eType,true,false,true);
        }*/
        ArrayList<String> vSources =
                (sourceFiles.size() == 1 && sourceFiles.get(0).equals("xargs"))
                        ? Utils.retrievingThroughXArgs(eType, false, true)
                        : Utils.retrieveFilePathTypes(new ArrayList<>(sourceFiles), eType, true, false, true);

        log.info("Scanning " + retrieveFullyQualifiedName(vSources).size() + " source file(s).");
        log.debug("Using the source file(s): " + retrieveFullyQualifiedName(vSources).toString());

        //检查dependency文件的路径
        log.info("Retrieving the dependency files.");
        List<String> vDeps = Utils.retrieveFilePathTypes(new ArrayList<>(dependencies), false,false,false);
        if(vDeps.size() > 0)
            log.debug("Using the dependency file(s): " + retrieveFullyQualifiedName(vDeps).toString());

        log.info("Retrieving the package path");
        List<String> basePath = new ArrayList<>();
        File sourceFile;
        String pkg = "";
        //检查源文件路径、源文件类型
        switch (eType){
            case APK:
            case WAR:
            case JAR:
                sourceFile = new File(sourceFiles.get(0));
                basePath.add(sourceFile.getName());
                pkg = sourceFile.getName();
                break;
            case DIR:
                sourceFile = new File(sourceFiles.get(0));
                basePath.add(sourceFile.getCanonicalPath() + ":dir");
                pkg = sourceFile.getName();
                break;
            case JAVAFILES:
            case CLASSFILES:
                for (String file : sourceFiles) {
                    sourceFile = new File(file);
                    basePath.add(sourceFile.getCanonicalPath());

                    if(pkg == null){
                        pkg = sourceFile.getCanonicalPath();
                    }
                }
                break;
        }
        log.debug("Package path: " + pkg);
        //使用源文件路径、类型、依赖项等信息进行初始化
        EnvironmentInfo info = new EnvironmentInfo(vSources,eType,oType,vDeps,basePath,pkg);

        //设置java路径
        info.setJavaHome(java);
        log.info("EnvironmentInfo setJavaHome: " + java);
        info.verifyBaseSetting();

        //设置main文件
        if(StringUtils.isNotEmpty(mainFile)){
            log.info("Attempting to validate the main method as " + mainFile);

            if(!info.getSource().contains(mainFile)){
                log.fatal("The main class path is not included within the source file.");
                throw new ExceptionHandler(
                        "The main class path is not included within the source file.", ExceptionId.ARG_VALID);
            }
            log.info("Using the main method from class " + mainFile);
            info.setMain(mainFile);
        }

        if(fileOutPath == null) {
            String packageName = info.getPackageName();
            String fileExtension = info.getMessagingType().getOutputFileExt();
            fileOutPath = Utils.getDefaultFileOut(info.getPackageName(),info.getMessagingType().getOutputFileExt());
            log.warn("Defaulting the output to file: " + fileOutPath);
        }

        info.setFileOut(fileOutPath);
        info.setJwtLibName(jwtLibName);
        log.info("set JwtLibName: " + info.getJwtLibName());
        info.setJwtInfoPath(info.getBuildRootDir().split(":")[0] + "/JWTResult.json");
        log.info("set JwtInfoPath: " + info.getJwtInfoPath());

        //region Specific Parameter Checking
/*        if (extraArguments != null && extraArguments.size() > 1)
            oType.retrieveSpecificArgHandler().inputValidation(info, extraArguments);*/
        //endregion

        return info;
    }



}
