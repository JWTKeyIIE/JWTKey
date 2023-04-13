package com.xu.utils;

import com.xu.analyzer.backward.MethodWrapper;
import com.xu.slicer.backward.property.PropertyAnalysisResult;
//import com.xu.slicer.PropertyInfluencingInstructions;
import com.xu.slicer.backward.property.PropertyInfluencingInstructions;
import soot.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字段初始化映射关系
 */
public class FieldInitializationInstructionMap {
    private static Map<String, List<PropertyAnalysisResult>> initializationInstructions = null;
    //  fieldVsMethodWrapper中的String为字段名FiledName，List<MethodWrapper>中为对应在方法体内对该字段的初始化方法；
    private static Map<String, List<MethodWrapper>> fieldVsMethodWrapper = null;

    public static void reset() {
        initializationInstructions = null;
        fieldVsMethodWrapper = null;
    }

    /**
     * build. 建立ClassName中的所有类的所有字段与类中对该字段进行实例化或者初始化的方法的对应关系 filedVsMethodWrapper
     *
     * @param classNames a {@link List} object.
     */
    public static void build(List<String> classNames) {
        if (fieldVsMethodWrapper == null) {

            fieldVsMethodWrapper = new HashMap<>();
            // 遍历所有的类
            for (String className : classNames) {

                SootClass sClass = Scene.v().loadClassAndSupport(className);
                //解析sclass类中所有的成员字段
                Chain<SootField> sootFields = sClass.getFields();
                //遍历类中的所有成员字段
                for (SootField field : sootFields) {

                    List<MethodWrapper> initMethods = new ArrayList<>();
                    //获取类中所有的方法
                    List<SootMethod> methodList = sClass.getMethods();
                    //遍历类中的所有方法
                    for (SootMethod method : methodList) {
                        //如果此方法不是幻像、抽象或native方法，即此方法可以有一个主体
                        if (method.isConcrete()) {

                            StringBuilder methodBody = new StringBuilder();

                            try {
                                //建立方法体
                                Body initBody = method.retrieveActiveBody();
                                //建立方法体内的控制流图 ExceptionUnitGraph类型的控制流图
                                UnitGraph graph = new ExceptionalUnitGraph(initBody);
                                //遍历方法体内的程序流图的节点
                                for (Object aGraph : graph) {
                                    methodBody.append(aGraph); //将所有的节点语句拼接成一个String
                                }
                                //如果方法体中包含filed字段字符串
                                if (methodBody.toString().contains(field.toString() + " =")) {
                                    //将该方法添加到initMethod列表中
                                    initMethods.add(NamedMethodMap.getMethod(method.toString()));
                                }
                            } catch (RuntimeException e) {
                                System.err.println(e);
                                continue;
                            }
                        }
                    }
                    fieldVsMethodWrapper.put(field.toString(), initMethods);
                }
            }
        }
    }

    /**
     * 获取初始化指令
     * @param fieldName 字段名  类名+方法名
     * @return
     */
    public static List<PropertyAnalysisResult> getInitInstructions(String fieldName) {

        //fieldVsMethodWrapper中包含所有载入的类以及方法和对应的签名
        if (fieldVsMethodWrapper == null) {
            throw new RuntimeException("Execute build first ...");
        }

        if (initializationInstructions == null) {
            initializationInstructions = new HashMap<>();
        }

        if (!initializationInstructions.containsKey(fieldName)) {

            List<PropertyAnalysisResult> analysisResultList = new ArrayList<>();

            initializationInstructions.put(fieldName, analysisResultList);

            //在fieldVsMethodWrapper中寻找filedName 对应的初始化方法列表
            List<MethodWrapper> initMethodList = fieldVsMethodWrapper.get(fieldName);

            if (initMethodList == null || initMethodList.isEmpty()) {
                return initializationInstructions.get(fieldName);

            } else {
                //遍历初始化方法列表
                for (MethodWrapper method : initMethodList) {
                    //在初始化方法中进行切片，fieldName 为切片准则
                    PropertyInfluencingInstructions simpleSlicerInstructions =
                            new PropertyInfluencingInstructions(method, fieldName);

                    PropertyAnalysisResult analysis = simpleSlicerInstructions.getSlicingResult();

                    if (!analysis.getSlicingResult().isEmpty()) {
                        analysisResultList.add(analysis);
                    }
                }
            }
        }
        return initializationInstructions.get(fieldName);
    }
}
