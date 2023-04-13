package com.xu.analyzer.ruleCheckers.checkRule;

import com.xu.analyzer.backward.Analysis;
import com.xu.analyzer.backward.AssignInvokeUnitContainer;
import com.xu.analyzer.backward.InvokeUnitContainer;
import com.xu.analyzer.backward.UnitContainer;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import com.xu.utils.Utils;
import org.apache.logging.log4j.Logger;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.RValueBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantKeyFinder {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(com.xu.analyzer.ruleCheckers.keyChecker.ConstantKeyFinder.class);
    public static final List<String> PREDICTABLE_SOURCES = new ArrayList<>();
    private final String rule = "2"; //ConstantKey


    private Map<UnitContainer, List<String>> othersSourceMap = new HashMap<>();
    private Map<UnitContainer, List<String>> predictableSourcMap = new HashMap<>();

    /**
     * com.auth0.jwt库有关对称密钥的切片参数
     */
    static {
        PREDICTABLE_SOURCES.add("<java.lang.System: long nanoTime()>");
        PREDICTABLE_SOURCES.add("<java.lang.System: long currentTimeMillis()>");
        PREDICTABLE_SOURCES.add("<java.util.Date: java.util.Date <init>");
    }
    public void ConstantKeyAnalysis(Analysis analysis){
        if (analysis.getAnalysisResult().isEmpty()){
            log.info("Analysis result is null");
            return;
        }
        log.info("Begin to analysis result");
        HashMap<String, List<String>> callerVsUsedConstants = new HashMap<>();

        //循环处理analysisResult中的语句
        for(int index = 0; index < analysis.getAnalysisResult().size(); index++) {

            UnitContainer e = analysis.getAnalysisResult().get(index);

            /**
             * 处理常数密钥问题
             */
            //如果当前语句是赋值语句并且不是赋值调用
            if (!(e instanceof AssignInvokeUnitContainer) && e.getUnit() instanceof JAssignStmt) {

                List<String> usedConstants = callerVsUsedConstants.get(e.getMethod());

                //先将现在语句的方法放入callerVsUsedConstants中
                //usedConstants是使用到的常量列表
                if(usedConstants == null) {
                    usedConstants = new ArrayList<>();
                    callerVsUsedConstants.put(e.getMethod(), usedConstants);
                }

                if (e.getUnit().toString().contains("interfaceinvoke ")) {

                    for (ValueBox useBox : e.getUnit().getUseBoxes()) {

                        //如果现在正在分析的语句中包含Constant 常量
                        if (useBox.getValue() instanceof Constant) {
                            usedConstants.add(useBox.getValue().toString());
                        }
                    }
                }
            }
            Map<UnitContainer, String> outSet = new HashMap<>();

            /**
             * 处理可以预测的不安全的源使用问题
             */
            //如果当前的语句是 赋值调用语句
            if (e instanceof AssignInvokeUnitContainer) {
                List<UnitContainer> resFromInside = ((AssignInvokeUnitContainer) e).getAnalysisResult();
                checkPredictableSourceFromInside(resFromInside, e, outSet);
            }else if (e instanceof InvokeUnitContainer) {
                List<UnitContainer> resFromInside = ((InvokeUnitContainer) e).getAnalysisResult();
                checkPredictableSourceFromInside(resFromInside, e, outSet);
            }else {
                for (String predictableSource : PREDICTABLE_SOURCES) {
                    if (e.getUnit().toString().contains(predictableSource)) {
                        outSet.put(e, e.toString());
                        break;
                    }
                }
            }

            if (e instanceof AssignInvokeUnitContainer) {
                List<UnitContainer> result = ((AssignInvokeUnitContainer) e).getAnalysisResult();

                if(result != null) {
                    for (UnitContainer unit : result) {
                        checkHeuristics(unit, outSet);
                    }
                }

            } else if (e instanceof InvokeUnitContainer) {
                List<UnitContainer> result = ((InvokeUnitContainer) e).getAnalysisResult();
                if (result != null) {
                    for (UnitContainer unit : result) {
                        checkHeuristics(unit, outSet);
                    }
                }

            } else {
                checkHeuristics(e, outSet);
            }
            if (outSet.isEmpty()) {
                continue;
            }

            // analysis 现在所有的分析结果，index，当前AnalysisResult的索引
            UnitContainer invokeResult = Utils.isArgumentOfInvoke(analysis, index, new ArrayList<>());

            if (invokeResult != null && invokeResult instanceof InvokeUnitContainer) {
                if ((((InvokeUnitContainer) invokeResult).getDefinedFields().isEmpty()
                        || !((InvokeUnitContainer) invokeResult).getArgs().isEmpty())
                        && invokeResult.getUnit().toString().contains("specialinvoke")) {
                    for (UnitContainer unitContainer : outSet.keySet()) {
                        putIntoMap(predictableSourcMap, unitContainer, outSet.get(unitContainer));
                    }
                }
            } else if (invokeResult != null && invokeResult.getUnit() instanceof JInvokeStmt) {
                if(invokeResult.getUnit().toString().contains("specialinvoke")) {

                    for (UnitContainer unitContainer : outSet.keySet()) {
                        putIntoMap(predictableSourcMap, unitContainer, outSet.get(unitContainer));
                    }
                }
                else {

                    for (UnitContainer unitContainer : outSet.keySet()) {
                        if (unitContainer.getUnit() instanceof JInvokeStmt
                                && unitContainer.getUnit().toString().contains("interfaceinvoke")) {

                            boolean found = false;

                            for (String constant : callerVsUsedConstants.get(e.getMethod())) {
                                if (((JInvokeStmt) unitContainer.getUnit())
                                        .getInvokeExpr()
                                        .getArg(0)
                                        .toString()
                                        .contains(constant)) {
                                    putIntoMap(predictableSourcMap, unitContainer, outSet.get(unitContainer));
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                putIntoMap(othersSourceMap, unitContainer, outSet.get(unitContainer));
                            }

                        }
                        else {
                            putIntoMap(othersSourceMap, unitContainer, outSet.get(unitContainer));
                        }
                    }
                }
            }
            else {

                for (UnitContainer unitContainer : outSet.keySet()) {

                    putIntoMap(predictableSourcMap, unitContainer, outSet.get(unitContainer));
                }
            }
        }
    }
    private void checkPredictableSourceFromInside(
            List<UnitContainer> result, UnitContainer e, Map<UnitContainer, String> outSet) {
        //循环处理 调用的函数中的Method中的语句
        for (UnitContainer key : result) {

            if (key instanceof AssignInvokeUnitContainer) {
                checkPredictableSourceFromInside(
                        ((AssignInvokeUnitContainer)key).getAnalysisResult(),key, outSet);
                continue;
            }

            if(key instanceof InvokeUnitContainer) {
                checkPredictableSourceFromInside(
                        ((InvokeUnitContainer) key).getAnalysisResult(), key, outSet);
                continue;
            }

            for(String predictableSource : PREDICTABLE_SOURCES) {
                if(key.getUnit().toString().contains(predictableSource)) {
                    outSet.put(e, e.toString());
                }
            }
        }
    }


    private void checkHeuristics(UnitContainer e, Map<UnitContainer, String> outSet) {

        if (e instanceof AssignInvokeUnitContainer) {
            for (UnitContainer u : ((AssignInvokeUnitContainer) e).getAnalysisResult()) {
                checkHeuristics(u, outSet);
            }
            return;
        }

        if (e instanceof InvokeUnitContainer) {
            for (UnitContainer u : ((InvokeUnitContainer) e).getAnalysisResult()) {
                checkHeuristics(u, outSet);
            }
            return;
        }

        for (ValueBox useBox : e.getUnit().getUseBoxes()) {
            if (useBox.getValue() instanceof Constant) {

                if (useBox.getValue().toString().equals("null")
                        || useBox.getValue().toString().equals("\"null\"")
                        || useBox.getValue().toString().equals("\"\"")
                        || useBox.getValue().toString().contains(" = class ")) {
                    putIntoMap(othersSourceMap, e, useBox.getValue().toString());
                    continue;
                }

                if (e.getUnit() instanceof JAssignStmt) {
                    if (((AssignStmt) e.getUnit()).containsInvokeExpr()) {
                        InvokeExpr invokeExpr = ((AssignStmt)e.getUnit()).getInvokeExpr();
                        List<Value> args = invokeExpr.getArgs();
                        for (Value arg : args) {
                            if (arg.equivTo(useBox.getValue())) {
                                putIntoMap(othersSourceMap, e, useBox.getValue().toString());
                                break;
                            }
                        }
                    }
                    else if (useBox.getValue().getType() instanceof IntegerType) {

                        List<ValueBox> defBoxes = e.getUnit().getDefBoxes();

                        if (defBoxes != null && !defBoxes.isEmpty()) {
                            if (useBox instanceof RValueBox
                                    && (defBoxes.get(0).getValue().getType() instanceof ByteType
                                    || defBoxes.get(0).getValue().getType() instanceof CharType)) {
                                outSet.put(e, useBox.getValue().toString());
                            } else {
                                putIntoMap(othersSourceMap, e, useBox.getValue().toString());
                            }
                        }

                    }
                    else {
                        if (useBox.getValue().getType() instanceof BooleanType
                                || useBox.getValue().getType() instanceof FloatType
                                || useBox.getValue().getType() instanceof DoubleType) {
                            putIntoMap(othersSourceMap, e, useBox.getValue().toString());
                        } else {
                            outSet.put(e, useBox.getValue().toString());
                        }
                    }
                }
                else if (e.getUnit().toString().contains(" newarray ")) {
                    putIntoMap(othersSourceMap, e, useBox.getValue().toString());
                }
                else {
                    if (useBox.getValue().getType() instanceof LongType
                            || useBox.getValue().toString().startsWith("\"")) {
                        outSet.put(e, useBox.getValue().toString());
                    }
                }
            }
        }
    }
    protected void putIntoMap(
            Map<UnitContainer, List<String>> unitStringMap, UnitContainer e, String value) {
        List<String> values = unitStringMap.get(e);
        if(values == null) {
            values = new ArrayList<>();
            values.add(value);
            unitStringMap.put(e, values);
            return;
        }
        if(!values.toString().contains(value)){
            values.add(value);
        }
    }
    public void createAnalysisOutput(
            Map<String, String> xmlFileStr, List<String> sourcePaths, OutputStructure output, String libName)
            throws ExceptionHandler {
        Utils.createAnalysisOutput_log(xmlFileStr, sourcePaths, predictableSourcMap, rule, output);
        Utils.createAnalysisOutput(xmlFileStr, sourcePaths, predictableSourcMap, rule, output,libName);
//        if(getLibName().contains("java-jwt") || getLibName().contains("jjwt") || getLibName().contains("FusionAuth") || getLibName().contains("vertx")){
//
//        }
    }
}
