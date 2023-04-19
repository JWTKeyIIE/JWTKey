package com.xu.utils;

import com.xu.analyzer.backward.MethodWrapper;
import com.xu.slicer.backward.MethodCallSiteInfo;
import org.apache.logging.log4j.Logger;
import soot.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NamedMethodMap {

    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(NamedMethodMap.class);

    //Map<K,V> Key为方法method Value为该方法对应的信息
    private static Map<String, MethodWrapper> nameVsMethodMap = null;

    private static boolean isCallerCalleeBuilt = false;
    /**
     * build.
     *
     * @param classNames a {@link List} object.
     */
    public static void build(List<String> classNames) {

        if (nameVsMethodMap == null) {

            nameVsMethodMap = new HashMap<>();

            for (String className : classNames) {
                SootClass sClass = Scene.v().getSootClass(className);

                fillMethodMapForClass(sClass);
            }
        }
    }

    /** clearCallerCalleeGraph. */
    public static void clearCallerCalleeGraph() {
        nameVsMethodMap = null;
        isCallerCalleeBuilt = false;
    }

    /**
     * addCriteriaClass.
     *
     * @param className a {@link String} object.
     */
    public static void addCriteriaClass(String className) {

        SootClass sClass = Scene.v().getSootClass(className);
        if (sClass.isPhantomClass()) {
            return;
        }

        fillMethodMapForClass(sClass);
    }

    public static void addInheritCriteriaClass(String className) {
        SootClass sootClass = Scene.v().getSootClass(className);
        if(sootClass.isPhantom()) {
            return;
        }
        fillMethodMapForSubClass(sootClass);

    }

    /**
     * 将切片类的子类添加到MethodMapForClass列表中
     * @param className
     */
    public static void addSubCriteriaClass(String className) {
        SootClass sootClass = Scene.v().getSootClass(className);
        if(sootClass.isPhantom()){
            log.info("addSubCriteriaClass: " + className.toString() + "is phantom class");
            return;
        }
        fillMethodMapForSubClass(sootClass);
    }

    /**
     * addCriteriaClasses.
     *
     * @param classNames a {@link List} object.
     */
    public static void addCriteriaClasses(List<String> classNames) {
        for (String clazz : classNames) {
            addCriteriaClass(clazz);
        }
    }

    /**
     * getMethod.
     *
     * @param methodName a {@link String} object.
     * @return a {@link MethodWrapper} object.
     */
    public static MethodWrapper getMethod(String methodName) {

        if (nameVsMethodMap == null) {
            throw new RuntimeException("Name vs Method Map is not built ...");
        } else {
            return nameVsMethodMap.get(methodName);
        }
    }

    /**
     * 获取SootClass类中的SootMethod，
     * 并将对应的Method加入nameVsMethodMap,
     * 其中包含方法名和methodWrapper（包含方法对应的调用信息）
     * @param sClass
     */
    private static void fillMethodMapForClass(SootClass sClass) {
        for (SootMethod m : sClass.getMethods()) {
            if (nameVsMethodMap.get(m.toString()) == null) {
                MethodWrapper methodWrapper = new MethodWrapper(m);
                nameVsMethodMap.put(m.toString(), methodWrapper);
            }
        }
    }

    private static void fillMethodMapForSubClass(SootClass sootClass) {
        for (SootMethod m : sootClass.getMethods()) {
            if (nameVsMethodMap.get(m.toString()) == null) {
                MethodWrapper methodWrapper = new MethodWrapper(m);
                nameVsMethodMap.put(m.toString(), methodWrapper);
            }
        }
        SootClass superClass = sootClass.getSuperclass();
        if(superClass.isPhantom() || superClass.getName().equals("java.lang.Object")) {
            return;
        }
        else {
            for (SootMethod m : superClass.getMethods()){
                String methodName ="<" + sootClass.getName() + ":" + m.toString().split(":")[1];
                if (nameVsMethodMap.get(methodName) == null) {
                    MethodWrapper methodWrapper = new MethodWrapper(m, true, sootClass.getName());
                    nameVsMethodMap.put(methodName, methodWrapper);
                }
            }
        }
    }

    /**
     * 将切片类的子类添加到MethodMap中
     * @param sootClass 切片类的子类
     */
/*    private static void fillMethodMapForSuperClass(SootClass sootClass) {
        for(SootMethod m : sootClass.getMethods()) {

        }
    }*/

    /**
     * buildCallerCalleeRelation. 创建每个类中所有方法之间的调用关系
     *
     * @param classNames a {@link List} object.
     */
    public static void buildCallerCalleeRelation(List<String> classNames) {

        if (isCallerCalleeBuilt) {
            return;
        }

        isCallerCalleeBuilt = true;

        Map<String, List<SootClass>> classHierarchy = Utils.getClassHierarchyAnalysis(classNames);

        for (String className : classNames) {
            SootClass sClass = Scene.v().getSootClass(className);

            //迭代Class中的所有方法
            Iterator methodIt = sClass.getMethods().iterator();
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

                    //获取该方法对应的MethodWrapper信息
                    MethodWrapper caller = NamedMethodMap.getMethod(m.toString());
                    //为每个给定的方法体Body构造关系实例图
                    UnitGraph graph = new ExceptionalUnitGraph(b);

                    Iterator gIt = graph.iterator();
                    //遍历方法实例图中的每个节点（每条语句）
                    while (gIt.hasNext()) {
                        Unit u = (Unit) gIt.next();
                        //ustr中为现在正在遍历到的语句的String表示
                        String uStr = u.toString();

                        if (uStr.contains("}") || uStr.contains("{") || uStr.contains(";")) {
                            continue;
                        }
                        //如果该语句包含静态调用或者是void<init>方法
                        if (uStr.contains("staticinvoke ") || uStr.contains("void <init>")) {
//              提取出被调用的方法
                            String invokedMethod = uStr.substring(uStr.indexOf('<'), uStr.lastIndexOf('>') + 1);
//              通过getMethod方法寻找被调用方法的相关信息。（如果是程序中需要被分析的类中的方法，那么这个方法应该被build过，所以可以通过getMethod找到相关信息，如果没有被build过，那么返回的callee为空）
                            MethodWrapper callee = NamedMethodMap.getMethod(invokedMethod);
//             当callee不为空（即被调用的方法是需要被分析的方法）并且Caller不等于Callee的时候（不是自身的调用的时候）
                            if (callee != null && caller != callee) {
                                //设置callee被调用方法的TopLevel参数为false（不是顶层的方法）
                                callee.setTopLevel(false);
//                创建新的方法调用关系信息
                                MethodCallSiteInfo callSiteInfo =
                                        new MethodCallSiteInfo(
                                                caller,
                                                callee,
                                                u.getJavaSourceStartLineNumber(),
                                                u.getJavaSourceStartColumnNumber());
//将方法调用关系信息保存在Caller方法的CalleeList中
                                caller.getCalleeList().add(callSiteInfo);
//                在被调用方法Callee的CallerList信息中添加caller方法（调用Callee方法的方法）
                                callee.getCallerList().add(caller);
                            }
//              如果语句中包含以下几种调用方式（针对对象或者类的调用）
                        } else if ((uStr.contains("virtualinvoke ")
                                || uStr.contains("interfaceinvoke ")
                                || uStr.contains("specialinvoke ")
                                || uStr.contains("dynamicinvoke "))
                                && uStr.contains(".<")) {
//              确定被调用的方法名
                            String invokedMethod = uStr.substring(uStr.indexOf('<'), uStr.lastIndexOf('>') + 1);
//              确定被调用的方法是哪个对象或者哪个类的方法
                            String reference = uStr.substring(uStr.indexOf("invoke ") + 7, uStr.indexOf(".<"));

                            String refType = null;
                            //获取reference的类型
                            for (ValueBox useBox : u.getUseBoxes()) {
                                if (useBox.getValue().toString().equals(reference)) {
                                    refType = useBox.getValue().getType().toString();
                                    break;
                                }
                            }
//              获取被调用方法的方法签名
                            String[] splits = invokedMethod.split(": ");
                            String methodSignature = splits[1].substring(0, splits[1].lastIndexOf('>'));
                            List<SootClass> subClasses = classHierarchy.get(refType);
//              查看该方法所在的类是否有子类，如果有的话需要将子类中对应的该方法也添加到CalleeList中
                            if (subClasses != null && !subClasses.isEmpty()) {

                                for (SootClass subClass : subClasses) {
                                    SootMethod subClassMethod = subClass.getMethodUnsafe(methodSignature);

                                    if (subClassMethod != null) {
                                        MethodWrapper callee = NamedMethodMap.getMethod(subClassMethod.toString());

                                        if (callee != null && caller != callee) {
                                            callee.setTopLevel(false);

                                            MethodCallSiteInfo callSiteInfo =
                                                    new MethodCallSiteInfo(
                                                            caller,
                                                            callee,
                                                            u.getJavaSourceStartLineNumber(),
                                                            u.getJavaSourceStartColumnNumber());
                                            caller.getCalleeList().add(callSiteInfo);
                                            callee.getCallerList().add(caller);
                                        }
                                    }
                                }
//                如果被调用的方法所在的类没有子类或者implement类
                            } else {
//                将被调用方法添加到CalleeList中
                                MethodWrapper callee = NamedMethodMap.getMethod(invokedMethod);

                                if (callee != null && caller != callee) {
                                    callee.setTopLevel(false);

                                    MethodCallSiteInfo callSiteInfo =
                                            new MethodCallSiteInfo(
                                                    caller,
                                                    callee,
                                                    u.getJavaSourceStartLineNumber(),
                                                    u.getJavaSourceStartColumnNumber());
                                    caller.getCalleeList().add(callSiteInfo);
                                    callee.getCallerList().add(caller);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
