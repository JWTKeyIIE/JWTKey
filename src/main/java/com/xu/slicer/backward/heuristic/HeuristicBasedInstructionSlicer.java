package com.xu.slicer.backward.heuristic;

import com.xu.analyzer.backward.UnitContainer;
import com.xu.slicer.ValueArraySparseSet;
import com.xu.slicer.backward.property.PropertyAnalysisResult;
import soot.ArrayType;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对赋值调用语句、调用语句等跨class、method的指令进行分析
 * 启发式指令切片
 */
public class HeuristicBasedInstructionSlicer extends BackwardFlowAnalysis {

    private static final List<String> ASSIGN_WHITE_LISTED_METHODS = new ArrayList<>();
    private static final List<String> INVOKE_WHITE_LISTED_METHODS = new ArrayList<>();
    private static final List<String> BLACK_LISTED_METHODS = new ArrayList<>();

    static {

        /**
         * 赋值调用方法白名单
         */
        ASSIGN_WHITE_LISTED_METHODS.add(
                "<javax.xml.bind.DatatypeConverterInterface: byte[] parseBase64Binary(java.lang.String)>");
        ASSIGN_WHITE_LISTED_METHODS.add(
                "<javax.xml.bind.DatatypeConverterInterface: byte[] parseHexBinary(java.lang.String)>");
        ASSIGN_WHITE_LISTED_METHODS.add("<java.util.Arrays: byte[] copyOf(byte[],int)>");

        /**
         * 调用方法白名单
         */
        INVOKE_WHITE_LISTED_METHODS.add(
                "<java.lang.System: void arraycopy(java.lang.Object,int,java.lang.Object,int,int)>");
        INVOKE_WHITE_LISTED_METHODS.add("<java.lang.String: void <init>");

        /**
         * 黑名单
         */
        BLACK_LISTED_METHODS.add("<javax.crypto.KeyGenerator: void <init>");
        BLACK_LISTED_METHODS.add("<javax.crypto.Cipher: void <init>");
    }

    private FlowSet emptySet;
    private String slicingCriteria;
    private String method;
    private Map<String, List<PropertyAnalysisResult>> propertyUseMap;


    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     *
     * @param graph
     */
    public HeuristicBasedInstructionSlicer(DirectedGraph graph, String slicingCriteria, String method) {
        super(graph);
        this.emptySet = new ValueArraySparseSet();
        this.slicingCriteria = slicingCriteria;
        this.method = method;
        this.propertyUseMap = new HashMap<>();
        doAnalysis();
    }


    @Override
    protected void flowThrough(Object in, Object node, Object out) {
        FlowSet inSet = (FlowSet) in, outSet = (FlowSet) out;
        Unit currInstruction = (Unit) node;

        if (currInstruction.toString().startsWith(slicingCriteria)) {
            addCurrInstInOutSet(outSet, currInstruction);
            return;
        }

        if(!inSet.isEmpty()) {

            outSet.union(inSet);

            if(currInstruction.toString().startsWith("if ")) {
                return;
            }

            for (Object anInSet : inSet.toList()) {

                UnitContainer insetInstruction = (UnitContainer) anInSet;
                List<ValueBox> useBoxes = insetInstruction.getUnit().getUseBoxes();

                for (ValueBox useBox : useBoxes) {

                    if ((useBox.getValue().toString().equals("r0")
                            && insetInstruction.getUnit().toString().contains("r0."))
                            || (useBox.getValue().toString().equals("this")
                            && insetInstruction.getUnit().toString().contains("this."))) {
                        continue;
                    }
                    if (isArgOfAssignInvoke(useBox, insetInstruction.getUnit())) {
                        continue;
                    }
                    if (insetInstruction.getUnit().toString().contains("[" + useBox.getValue() + "]")) {
                        continue;
                    }

                    if (isSpecialInvokeOn(currInstruction, useBox)) {
                        addCurrInstInOutSet(outSet, currInstruction);
                        return;
                    }

                    for (ValueBox defBox : currInstruction.getDefBoxes()) {

                        if ((defBox.getValue().toString().equals("r0")
                                && currInstruction.toString().startsWith("r0."))
                                || (defBox.getValue().toString().equals("this")
                                && currInstruction.toString().startsWith("this."))) {
                            continue;
                        }

                        if(defBox.getValue().equivTo(useBox.getValue())) {
                            addCurrInstInOutSet(outSet, currInstruction);
                            return;
                        }else if (defBox.getValue().toString().contains(useBox.getValue().toString())) {
                            if (useBox.getValue().getType() instanceof ArrayType) {
                                addCurrInstInOutSet(outSet, currInstruction);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void addCurrInstInOutSet (FlowSet outSet, Unit currInstruction) {
        UnitContainer currUnitContainer = new UnitContainer();
        currUnitContainer.setUnit(currInstruction);
        currUnitContainer.setMethod(method);

        outSet.add(currUnitContainer);
    }

    private boolean isSpecialInvokeOn(Unit currInstruction, ValueBox useBox) {
        boolean specialinvoke =
                currInstruction instanceof JInvokeStmt
                && currInstruction.toString().contains("specialinvoke")
                && currInstruction.toString().contains(useBox.getValue().toString() + ".<");

        for (String whitelisted : INVOKE_WHITE_LISTED_METHODS) {
            if(currInstruction.toString().contains(whitelisted)
            && currInstruction.toString().contains(useBox.getValue().toString() + ",")) {
                specialinvoke = true;
                break;
            }
        }
        return specialinvoke;
    }

    private boolean isArgOfAssignInvoke(ValueBox useBox, Unit unit) {

        for(String blacklisted : BLACK_LISTED_METHODS) {
            if (unit instanceof JInvokeStmt && unit.toString().contains(blacklisted)) {
                return true;
            }
        }
        //如果当前语句是赋值语句，并且为赋值调用
        if (unit instanceof JAssignStmt && unit.toString().contains("invoke ")) {

            for (String whitelisted : ASSIGN_WHITE_LISTED_METHODS) {
                if (unit.toString().contains(whitelisted)) {
                    return false;
                }
            }

            InvokeExpr invokeExpr = ((JAssignStmt) unit).getInvokeExpr();
            List<Value> args = invokeExpr.getArgs();
            for (Value arg : args) {
                if (arg.equivTo(useBox.getValue())) {
                    return true;
                }
            }
        }

        if(unit.toString().contains(" newarray ")) {
            for (ValueBox valueBox : unit.getUseBoxes()) {
                if (valueBox.getValue().equivTo(useBox.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    protected Object entryInitialFlow() {
        return emptySet.clone();
    }

    @Override
    protected Object newInitialFlow() {
        return emptySet.clone();
    }

    @Override
    protected void merge(Object in1, Object in2, Object out) {
        FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
        inSet1.union(inSet2, outSet);
    }

    @Override
    protected void copy(Object source, Object dest) {
        FlowSet srcSet = (FlowSet) source, destSet = (FlowSet) dest;
        srcSet.copy(destSet);
    }
    public Map<String, List<PropertyAnalysisResult>> getPropertyUseMap() {
        return propertyUseMap;
    }
}
