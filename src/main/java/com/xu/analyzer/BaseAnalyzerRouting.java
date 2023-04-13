package com.xu.analyzer;

import com.xu.analyzer.ruleCheckers.BaseRuleChecker;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.JimpleBody;
import soot.options.Options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.xu.utils.Utils.*;


public class BaseAnalyzerRouting {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(BaseAnalyzerRouting.class);

    public static void environmentRouting(
            SourceType routingType,
            String criteriaClass,
            String criteriaMethod,
            int criteriaParam,
            List<String> snippetPath,
            List<String> projectDependency,
            BaseRuleChecker checker,
            String mainKlass,
            String androidHome,
            String javaHome,
            String jwtInfoPath)
            throws ExceptionHandler, IOException {

        switch (routingType) {
            case APK:
                setupBaseAPK(
                        criteriaClass,
                        criteriaMethod,
                        criteriaParam,
                        snippetPath.get(0),
                        checker,
                        mainKlass,
                        androidHome,
                        javaHome);
                break;
            case WAR:
                setupBaseWar(
                        criteriaClass,
                        criteriaMethod,
                        criteriaParam,
                        snippetPath.get(0),
                        projectDependency.size() >= 1 ? projectDependency.get(0) : null,
                        checker,
                        mainKlass,
                        javaHome
                );
                break;
            case JAR:
                //根据不同的项目类型选择不同的处理函数
                setupBaseJar(
                        criteriaClass,
                        criteriaMethod,
                        criteriaParam,
                        snippetPath.get(0),
                        projectDependency.size() >= 1 ? projectDependency.get(0) : null,
                        checker,
                        mainKlass,
                        javaHome);
                break;
            case DIR:
                setupBaseDir(
                        criteriaClass,
                        criteriaMethod,
                        criteriaParam,
                        snippetPath,
                        projectDependency,
                        checker,
                        mainKlass,
                        javaHome,
                        jwtInfoPath);
                break;
        }
    }

    public static void setupBaseAPK(String criteriaClass,
                                    String criteriaMethod,
                                    int criteriaParam,
                                    String projectJarPath,
                                    BaseRuleChecker checker,
                                    String mainKlass,
                                    String androidHome,
                                    String javaHome) throws ExceptionHandler {
        List<String> classNames = Utils.getClassNamesFromApkArchive(projectJarPath);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_android_jars(Utils.osPathJoin(androidHome, "platforms"));
        Options.v().set_soot_classpath(Utils.getBaseSoot(javaHome));

        Options.v().set_process_dir(Collections.singletonList(projectJarPath));
        Options.v().set_whole_program(true);

        loadBaseSootInfo(classNames, criteriaClass, criteriaMethod, criteriaParam, checker, "_APK_", null);
    }

    public static void setupBaseWar(
            String criteriaClass,
            String criteriaMethod,
            int criteriaParam,
            String projectWarPath,
            String projectDependencyPath,
            BaseRuleChecker checker,
            String mainKlass,
            String javaHome
    ) throws ExceptionHandler {
        List<String> classNames = Utils.getClassNamesFromWarArchive(projectWarPath);
        /**
         * 处理WAR包，将Classes打包成Jar包，将Lib中的内容打包到dependencies，
         */
        String projectJarPath = parseWarToJar(projectWarPath);


        Scene.v()
                .setSootClassPath(
                        Utils.join(
                                ";",
                                projectJarPath,
                                Utils.getBaseSoot(javaHome),
                                Utils.join(";", Utils.getJarsInDirectory(projectDependencyPath))));
        log.info("Setting the soot class path as: " + Scene.v().getSootClassPath());

        loadBaseSootInfo(classNames, criteriaClass, criteriaMethod, criteriaParam, checker, "_WAR_", null);
        //删除temp目录
    }

    public static void setupBaseJar(String criteriaClass,
                                    String criteriaMethod,
                                    int criteriaParam,
                                    String projectJarPath,
                                    String projectDependencyPath,
                                    BaseRuleChecker checker,
                                    String mainKlass,
                                    String javaHome) throws ExceptionHandler {
        List<String> classNames = Utils.getClassNamesFromJarArchive(projectJarPath);

        for (String dependency : Utils.getJarsInDirectory(projectDependencyPath))
            classNames.addAll(Utils.getClassNamesFromJarArchive(dependency));


        Scene.v()
                .setSootClassPath(
                        Utils.join(
                                ";",
                                projectJarPath,
                                Utils.getBaseSoot(javaHome),
                                Utils.join(";", Utils.getJarsInDirectory(projectDependencyPath))));
        log.info("Setting the soot class path as: " + Scene.v().getSootClassPath());

        loadBaseSootInfo(classNames, criteriaClass, criteriaMethod, criteriaParam, checker, "_JAR_", null);

    }

    public static void setupBaseDir(
            String criteriaClass,
            String criteriaMethod,
            int criteriaParam,
            List<String> snippetPath,
            List<String> projectDependency,
            BaseRuleChecker checker,
            String mainKlass,
            String javaHome,
            String jwtInfoPath)
            throws ExceptionHandler {
        log.debug("Xu: This is Source project analysis");
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_src_prec(Options.src_prec_java);

        Scene.v()
                .setSootClassPath(
                        Utils.getBaseSoot(javaHome)
                                + ";"
                                + Utils.join(";", snippetPath)
                                + ";"
                                + buildSootClassPath(projectDependency));
        log.debug("javaHome: " + javaHome);
        log.debug("soot path:" + Utils.getBaseSoot(javaHome)
                + ";"
                + Utils.join(";", snippetPath)
                + ";"
                + buildSootClassPath(projectDependency));
        log.debug("Utils.getBaseSoot(javaHome)" + Utils.getBaseSoot(javaHome));
        log.debug("Utils.join(\";\", snippetPath)" + Utils.join(";", snippetPath));
        log.debug("Utils.buildSootClassPath(projectDependency)" + buildSootClassPath(projectDependency));
        List<String> classNames = Utils.getClassNamesFromSnippet(snippetPath);
        List<String> containJwtClassName = new ArrayList<>();

        try {
            if (getLibraryJwtUseInfo(jwtInfoPath) != null) {
                containJwtClassName = getJwtContainClassName(Utils.getLibraryJwtUseInfo(jwtInfoPath).getFullPath());
            } else {
                containJwtClassName = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadBaseSootInfo(classNames, criteriaClass, criteriaMethod, criteriaParam, checker, "_DIR_", containJwtClassName);
    }

    public static void loadBaseSootInfo(
            List<String> classNames,
            String criteriaClass,
            String criteriaMethod,
            int criteriaParam,
            BaseRuleChecker checker,
            String mainKlass,
            List<String> jwtContainClass)
            throws ExceptionHandler {

        Options.v().set_keep_line_number(true);
        Options.v().set_allow_phantom_refs(true);
        List<String> ignoreLibs =
                Arrays.asList("okhttp3.Request$Builder", "retrofit2.Retrofit$Builder");
        for (String clazz : checker.getCriteriaClasses()) {
            log.debug("Loading with the class: " + clazz);
            try {
                SootClass runningClass;
                if ((runningClass = Scene.v().loadClassAndSupport(clazz)).isPhantom()
                        && !ignoreLibs.contains(runningClass.getName())) {
                    log.fatal("Class: " + clazz + " is not properly loaded");
                    throw new ExceptionHandler(
                            "Class: " + clazz + " is not properly loaded", ExceptionId.LOADING);
                }
                log.debug("Successfully loaded the class: " + clazz);
/*                SootClass sc = Scene.v().getSootClass(clazz);
                if (sc.hasSuperclass()){
                    SootClass superClass = sc.getSuperclass();
                    log.info("supherClass:" + superClass.getName());
                }*/
            } catch (ExceptionHandler e) {
                throw e;
            } catch (Error | Exception e) {
                log.fatal("Error loading Class: " + clazz);
                throw new ExceptionHandler("Error loading Class: " + clazz, ExceptionId.LOADING);
            }
        }

        boolean mainMethodFound = false;
        boolean avoidMainKlass =
                StringUtils.isNotEmpty(mainKlass)
                        && !mainKlass.equals("_JAR_")
                        && !mainKlass.equals("_APK_")
                        && !mainKlass.equals("_DIR_");

        if (jwtContainClass != null && !jwtContainClass.isEmpty()) {
            for (String jwtClass : jwtContainClass) {
                for (String clazz : classNames) {
                    if (clazz.contains(jwtClass)) {
                        try {
                            SootClass runningClass = Scene.v().loadClassAndSupport(clazz);
                            if (runningClass.isPhantom()) {
                                log.fatal("Class: " + clazz + " is not properly loaded");
                                throw new ExceptionHandler(
                                        "Class " + clazz + " is not properly loaded", ExceptionId.LOADING);
                            }
                            SootClass jwtclass = Scene.v().getSootClass(clazz);
                            for (SootMethod m : jwtclass.getMethods()) {
                                JimpleBody body = (JimpleBody) m.retrieveActiveBody();
                                log.info("Method signature: " + m.getSignature() + "\nbody:" + m.getActiveBody().toString());
                            }
                            boolean containsMain =
                                    runningClass.getMethods().stream().anyMatch(m -> m.getName().equals("main"));
                            if (!mainMethodFound) mainMethodFound = containsMain;
                            else if (avoidMainKlass && containsMain && StringUtils.isEmpty(mainKlass)) {
                                log.fatal("Multiple Entry-points (main) found within the files included.");
                                throw new ExceptionHandler(
                                        "Multiple Entry-points (main) found within the files included.",
                                        ExceptionId.FILE_READ);
                            }
                            log.debug("Successfully loaded the Class: " + clazz);
                        } catch (ExceptionHandler e) {
                            throw e;
                        } catch (Error | Exception e) {
                            log.fatal("Error loading class " + clazz);
                            throw new ExceptionHandler("Error loading class " + clazz, ExceptionId.LOADING);
                        }

                    }

                }
            }
        } else {
            for (String clazz : classNames) {
                try {
                    SootClass runningClass = Scene.v().loadClassAndSupport(clazz);
                    if (runningClass.isPhantom()) {
                        log.fatal("Class: " + clazz + " is not properly loaded");
                        throw new ExceptionHandler(
                                "Class " + clazz + " is not properly loaded", ExceptionId.LOADING);
                    }

                    boolean containsMain =
                            runningClass.getMethods().stream().anyMatch(m -> m.getName().equals("main"));
                    if (!mainMethodFound) mainMethodFound = containsMain;
                    else if (avoidMainKlass && containsMain && StringUtils.isEmpty(mainKlass)) {
                        log.fatal("Multiple Entry-points (main) found within the files included.");
                        throw new ExceptionHandler(
                                "Multiple Entry-points (main) found within the files included.",
                                ExceptionId.FILE_READ);
                    }
                    log.debug("Successfully loaded the Class: " + clazz);

                } catch (ExceptionHandler e) {
                    throw e;
                } catch (Error | Exception e) {
                    log.fatal("Error loading class " + clazz);
                    throw new ExceptionHandler("Error loading class " + clazz, ExceptionId.LOADING);
                }
            }
        }

        Scene.v().loadNecessaryClasses();
        Scene.v().setDoneResolving();
        Options.v().set_prepend_classpath(true);
        Options.v().set_no_bodies_for_excluded(true);

        if ((StringUtils.isNotEmpty(mainKlass) && avoidMainKlass)
                && (!Scene.v().hasMainClass()
                || classNames
                .stream()
                .noneMatch(str -> str.equals(Scene.v().getMainClass().getName())))) {
            log.fatal(
                    "Could not detected an entry-point (main method) within any of the files provided.");
            throw new ExceptionHandler(
                    "Could not detected an entry-point (main method) within any of the files provided.",
                    ExceptionId.FILE_READ);
        }

        if (StringUtils.isNotEmpty(mainKlass)
                && avoidMainKlass
                && !Scene.v().getMainClass().getName().equals(mainKlass)) {
            SootClass mainClass = null;
            try {
                mainClass = Scene.v().getSootClass(Utils.retrieveFullyQualifiedName(mainKlass));
            } catch (RuntimeException e) {
                log.fatal("The class " + mainKlass + " was not loaded correctly.");
                throw new ExceptionHandler(
                        "The class " + mainKlass + " was not loaded correctly.", ExceptionId.LOADING);
            }
            try {
                Scene.v().setMainClass(mainClass);
            } catch (RuntimeException e) {
                log.fatal("The class " + mainKlass + " does not have a main method.");
                throw new ExceptionHandler(
                        "The class " + mainKlass + " does not have a main method.", ExceptionId.LOADING);
            }
        }

        String endPoint = "<" + criteriaClass + ": " + criteriaMethod + ">";
        ArrayList<Integer> slicingParameters = new ArrayList<>();
        slicingParameters.add(criteriaParam);

        log.debug("Starting the slicer");
/*        BaseAnalyzer.analyzeSliceInternal2(
                criteriaClass, classNames, endPoint, slicingParameters, checker,criteriaMethod);*/
        BaseAnalyzer.analyzeSliceInternal(criteriaClass, classNames, endPoint, slicingParameters, checker);
    }

}
