package com.xu.analyzer;

import com.xu.analyzer.differentEntry.SourceEntry;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.utils.Utils;
import soot.Scene;
import soot.options.Options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UniqueRuleAnalyzer {
    public static List<String> environmentRouting(
            List<String> projectJarPath,
            List<String> projectDependencyPath,
            SourceType routingType,
            String androidHome,
            String javaHome)
            throws ExceptionHandler {
        if (routingType == SourceType.JAR) {
            return setupBaseJarEnv(
                    projectJarPath.get(0),
                    projectDependencyPath.size() >= 1 ? projectDependencyPath.get(0) : null,
                    javaHome);
//        } else if (routingType == SourceType.APK) {
//            return setupBaseAPKEnv(projectJarPath.get(0), androidHome, javaHome);
        } else if (routingType == SourceType.DIR) {
            return setupBaseSourceEnv(projectJarPath, projectDependencyPath, javaHome);
        } else if (routingType == SourceType.JAVAFILES) {
            return setupJavaFileEnv(projectJarPath, projectDependencyPath, javaHome);
        } else { //if (routingType == EngineType.JAVACLASSFILES)
            return setupJavaClassFileEnv(projectJarPath, projectDependencyPath, javaHome);
        }
    }
    public static List<String> setupBaseJarEnv(
            String projectJarPath, String projectDependencyPath, String javaHome)
            throws ExceptionHandler {

        List<String> sootPaths = new ArrayList<>();
        sootPaths.add(projectJarPath);
        sootPaths.add(Utils.osPathJoin(javaHome, "jre", "lib", "rt.jar"));
        sootPaths.add(Utils.osPathJoin(javaHome, "jre", "lib", "jce.jar"));

        if (projectDependencyPath != null) sootPaths.add(projectJarPath);

        Scene.v().setSootClassPath(Utils.buildSootClassPath(sootPaths));

        Utils.loadSootClasses(null);

        return Utils.getClassNamesFromJarArchive(projectJarPath);
    }

    /**
     * setupBaseAPKEnv.
     *
     * @param projectJarPath a {@link String} object.
     * @param androidHome a {@link String} object.
     * @param javaHome a {@link String} object.
     * @return a {@link List} object.
     * @throws ExceptionHandler if any.
     */
/*    public static List<String> setupBaseAPKEnv(
            String projectJarPath, String androidHome, String javaHome) throws ExceptionHandler {

        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_android_jars(Utils.osPathJoin(androidHome, "platforms"));
        Options.v().set_soot_classpath(Utils.getBaseSoot(javaHome));

        Options.v().set_process_dir(Collections.singletonList(projectJarPath));
        Options.v().set_whole_program(true);

        Utils.loadSootClasses(null);

        return Utils.getClassNamesFromApkArchive(projectJarPath);
    }*/

    /**
     * setupBaseSourceEnv.
     *
     * @param snippetPath a {@link List} object.
     * @param projectDependencyPath a {@link List} object.
     * @param javaHome a {@link String} object.
     * @return a {@link List} object.
     * @throws ExceptionHandler if any.
     */
    public static List<String> setupBaseSourceEnv(
            List<String> snippetPath, List<String> projectDependencyPath, String javaHome)
            throws ExceptionHandler {

        List<String> classNames = Utils.getClassNamesFromSnippet(snippetPath);

        String srcPaths = String.join(":", snippetPath);

        Options.v()
                .set_soot_classpath(
                        Utils.getBaseSoot(javaHome)
                                + ":"
                                + srcPaths
                                + Utils.buildSootClassPath(projectDependencyPath));

        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_src_prec(Options.src_prec_java);

        Utils.loadSootClasses(classNames);

        return classNames;
    }

    //region Java Files
    //Like Dir

    /**
     * setupJavaFileEnv.
     *
     * @param snippetPath a {@link List} object.
     * @param projectDependencyPath a {@link List} object.
     * @param javaHome a {@link String} object.
     * @return a {@link List} object.
     * @throws ExceptionHandler if any.
     */
    public static List<String> setupJavaFileEnv(
            List<String> snippetPath, List<String> projectDependencyPath, String javaHome)
            throws ExceptionHandler {

        List<String> classNames = Utils.retrieveFullyQualifiedName(snippetPath);

        StringBuilder sootPath = new StringBuilder();
        sootPath.append(Utils.getBaseSoot(javaHome)).append(":").append(String.join(":", snippetPath));

        if (projectDependencyPath.size() >= 1) {
            List<String> classPaths = Utils.retrieveTrimmedSourcePaths(snippetPath);
            sootPath.append(
                    Utils.buildSootClassPath(
                            Utils.retrieveBaseSourcePath(classPaths, projectDependencyPath.get(0))));
        }

        Options.v().set_soot_classpath(sootPath.toString());

        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_src_prec(Options.src_prec_java);

        Utils.loadSootClasses(classNames);

        return classNames;
    }
    //endregion

    //region JavaClassFiles
    //Like Jar

    /**
     * setupJavaClassFileEnv.
     *
     * @param javaClassFiles a {@link List} object.
     * @param projectDependencyPath a {@link List} object.
     * @param javaHome a {@link String} object.
     * @return a {@link List} object.
     * @throws ExceptionHandler if any.
     */
    public static List<String> setupJavaClassFileEnv(
            List<String> javaClassFiles, List<String> projectDependencyPath, String javaHome)
            throws ExceptionHandler {

        Scene.v().setSootClassPath(Utils.getBaseSoot(javaHome));

        List<String> classNames = new ArrayList<>();
        for (String in : javaClassFiles) classNames.add(Utils.retrieveFullyQualifiedName(in));

        Utils.loadSootClasses(null);

        return classNames;
    }
    //endregion
}
