package com.xu.PrepareProcess.criteriagain;

import com.xu.analyzer.backward.MethodWrapper;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import com.xu.analyzer.differentEntry.EntryHandler;
import com.xu.environmentInit.EnvironmentInfo;
import com.xu.utils.NamedMethodMap;
import com.xu.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JarCriteriaEntry implements EntryHandler {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(JarCriteriaEntry.class);

    @Override
    public void Scan(EnvironmentInfo info) throws ExceptionHandler {
        String projectDependencyPath = null;
        if(info.getDependencies().size() >= 1){
            projectDependencyPath = info.getDependencies().get(0);
        }
        log.debug("Begin Analysis Criteria Class and method");
        List<String> projectPaths = info.getSource();
        String projectJarPath = projectPaths.get(0);
        List<String> classNames = Utils.getClassNamesFromJarArchive(projectJarPath);

        for (String dependency : Utils.getJarsInDirectory(projectDependencyPath))
            classNames.addAll(Utils.getClassNamesFromJarArchive(dependency));
        Scene.v().setSootClassPath(
                Utils.join(
                        ";",
                        projectJarPath,
                        Utils.getBaseSoot(info.getJavaHome()),
                        Utils.join(";", Utils.getJarsInDirectory(projectDependencyPath))));
        log.info("Setting the soot class path as: " + Scene.v().getSootClassPath());

        log.debug("Begin load Soot class");
        String criteriaClass = "RSA1_5";
        loadBaseSootInfo(classNames,null,criteriaClass);
    }

    public static void loadBaseSootInfo(List<String> classNames, String mainKlass,String criteriaClass) throws ExceptionHandler {
        Options.v().set_keep_line_number(true);
        Options.v().set_allow_phantom_refs(true);
        boolean mainMethodFound = false;
        boolean avoidMainKlass =
                StringUtils.isNotEmpty(mainKlass)
                        && !mainKlass.equals("_JAR_")
                        && !mainKlass.equals("_APK_")
                        && !mainKlass.equals("_DIR_");

        for (String clazz : classNames) {
            log.debug("Working with the internal class path: " + clazz);
            try {
                SootClass runningClass = Scene.v().loadClassAndSupport(clazz);
                if (runningClass.isPhantom()) {
                    log.fatal("Class: " + clazz + " is not properly loaded");
                    throw new ExceptionHandler(
                            "Class " + clazz + " is not properly loaded", ExceptionId.LOADING);
                }
                if(clazz.contains(criteriaClass)){
                    List<SootMethod> methodList = runningClass.getMethods();
                    List<String> methodName = new ArrayList<>();
                    for(SootMethod method : methodList){
                        methodName.add(method.getSignature());
                        log.info("method signature: " + method.getSignature());
                    }
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
        Scene.v().loadNecessaryClasses();
        Scene.v().setDoneResolving();
        Options.v().set_prepend_classpath(true);
        Options.v().set_no_bodies_for_excluded(true);

        getCriteriaClassMethod("java.util.random");
    }
    private static void getCriteriaClassMethod(String criteriaClass) {
        SootClass runningClass = Scene.v().getSootClass(criteriaClass);
        if (runningClass.isPhantom()) {
            log.info("error load the class");
            return;
        } else {
            Iterator methodIt = runningClass.getMethods().iterator();
            while (methodIt.hasNext()) {
                SootMethod m = (SootMethod) methodIt.next();

                //如果方法m不是抽象方法、抽象方法或者是本地方法，那么可以创建方法体Body
                if (m.isConcrete()) {
                    Body b;
                    try {
                        b = m.retrieveActiveBody();
                    } catch (RuntimeException e) {
                        System.err.println(e);
                        continue;
                    }
                    UnitGraph graph = new ExceptionalUnitGraph(b);
                    System.out.println(b.toString());
                }
            }
        }
    }
}
