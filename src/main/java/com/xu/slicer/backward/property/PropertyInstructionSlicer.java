package com.xu.slicer.backward.property;

import com.xu.analyzer.backward.UnitContainer;
import com.xu.analyzer.backward.AssignInvokeUnitContainer;
import com.xu.analyzer.backward.InvokeUnitContainer;
import com.xu.analyzer.backward.PropertyFakeUnitContainer;
import com.xu.slicer.ValueArraySparseSet;
import com.xu.utils.FieldInitializationInstructionMap;
import com.xu.utils.Utils;
import soot.ArrayType;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.baf.internal.BafLocal;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

import java.util.*;

public class PropertyInstructionSlicer extends BackwardFlowAnalysis {

    private FlowSet emptySet;
    private String slicingCriteria;
    private String initMethod;
    private List<String> usedFields;
    private Map<String, List<PropertyAnalysisResult>> propertyUseMap;

    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     *
     * @param graph
     */
    public PropertyInstructionSlicer(DirectedGraph graph, String slicingCriteria, String initMethod) {
        super(graph);
        this.emptySet = new ValueArraySparseSet();
        this.slicingCriteria = slicingCriteria;
        this.initMethod = initMethod;
        usedFields = new ArrayList<>();
        this.propertyUseMap = new HashMap<>();
        doAnalysis();
    }


    @Override
    protected void flowThrough(Object in, Object node, Object out) {

        FlowSet inSet = (FlowSet) in, outSet = (FlowSet) out;
        Unit currInstruction = (Unit) node;

        if (currInstruction.toString().contains(slicingCriteria)) {
            //如果当前指令为赋值语句，并且定义了一个变量
            if (currInstruction.getDefBoxes().size() == 1) {
                ValueBox defBox = currInstruction.getDefBoxes().get(0);
                //如果赋值语句包含切片准则
                if (defBox.getValue().toString().contains(slicingCriteria)) {
                    //如果当前语句使用的值只有一个
                    /**
                     * 例如：
                     *  initMethod： <tester.NewTestCase2: void go3()>
                     *  currInstruction: <tester.NewTestCase2: char[] encryptKey> = $r0
                     */
                    if (currInstruction.getUseBoxes().size() == 1) {
                        Value localValue =
                                new BafLocal(
                                        "$fakeLocal_" + defBox.getValue().toString(),
                                        currInstruction.getUseBoxes().get(0).getValue().getType());
                        AssignStmt assignStmt =
                                Jimple.v()
                                        .newAssignStmt(localValue, currInstruction.getUseBoxes().get(0).getValue());
                        addFakeInstInOutSet(outSet, assignStmt, defBox.getValue().toString(), inSet);
                        return;
                    } else if (currInstruction.getUseBoxes().size() > 1) {
                        //如果当前的指令为调用指令
                        if (currInstruction.toString().contains("invoke ")) {
                            for (ValueBox useBox : currInstruction.getUseBoxes()) {
                                if (useBox.getValue().toString().contains("invoke ")) {
                                    Value localValue =
                                            new BafLocal(
                                                    "$fakeLocal_" + defBox.getValue().toString(),
                                                    useBox.getValue().getType());
                                    AssignStmt assignStmt = Jimple.v().newAssignStmt(localValue, useBox.getValue());
                                    addFakeInstInOutSet(outSet, assignStmt, defBox.getValue().toString(), inSet);
                                    return;
                                }
                            }
                        }
                        /**
                         * 例如：r0.<tester.SymCrypto: java.lang.String defaultKey> = r6 ,
                         *      this.defaultKey = keyStr
                         */
                        else if (currInstruction.getUseBoxes().size() == 2) {
                            // localValue: $fakeLocal_r0.<tester.SymCrypto: java.lang.String defaultKey>
                            Value localValue =
                                    new BafLocal(
                                            "$fakeLocal_" + defBox.getValue().toString(),
                                            currInstruction.getUseBoxes().get(1).getValue().getType());
                            // assignStmt: $fakeLocal_r0.<tester.SymCrypto: java.lang.String defaultKey> = r6
                            AssignStmt assignStmt =
                                    Jimple.v()
                                            .newAssignStmt(localValue, currInstruction.getUseBoxes().get(1).getValue());
                            addFakeInstInOutSet(outSet, assignStmt, defBox.getValue().toString(), inSet);
                            return;
                        }
                    }
                }
            }
        }
        if (!inSet.isEmpty()) {

            outSet.union(inSet);

            if (currInstruction.toString().startsWith("if ")) {
                return;
            }

            for (Object anInSet : inSet.toList()) {
                UnitContainer insetInstruction = (UnitContainer) anInSet;
                List<ValueBox> useBoxes = insetInstruction.getUnit().getUseBoxes();

                for (ValueBox usebox : useBoxes) {

                    if ((usebox.getValue().toString().equals("r0")
                            && insetInstruction.getUnit().toString().contains("r0."))
                            || (usebox.getValue().toString().equals("this")
                            && insetInstruction.getUnit().toString().contains("this."))) {
                        continue;
                    }

                    if (insetInstruction instanceof AssignInvokeUnitContainer) {

                        int arg = Utils.isArgOfAssignInvoke(usebox, insetInstruction.getUnit());

                        if (arg > -1) {
                            String args = ((AssignInvokeUnitContainer) insetInstruction).getArgs().toString();

                            if (!args.contains("" + arg)) {
                                continue;
                            }
                        }
                    }

                    if (insetInstruction instanceof InvokeUnitContainer) {

                        int arg = Utils.isArgOfInvoke(usebox, insetInstruction.getUnit());

                        if (arg > -1) {
                            String args = ((InvokeUnitContainer) insetInstruction).getArgs().toString();

                            if (!args.contains("" + arg)) {
                                continue;
                            }
                        }
                    }

                    if (Utils.isArgOfByteArrayCreation(usebox, insetInstruction.getUnit())) {
                        continue;
                    }

                    if (insetInstruction.getUnit().toString().contains("[" + usebox.getValue() + "]")) {
                        continue;
                    }

                    if (isInvokeOn(currInstruction, usebox)) {
                        addCurrInstInOutSet(outSet, currInstruction);
                        return;
                    }

                    for (ValueBox defbox : currInstruction.getDefBoxes()) {

                        if ((defbox.getValue().toString().equals("r0")
                                && currInstruction.toString().startsWith("r0."))
                                || (defbox.getValue().toString().equals("this")
                                && currInstruction.toString().startsWith("this."))) {
                            continue;
                        }

                        if (defbox.getValue().equivTo(usebox.getValue())) {
                            addCurrInstInOutSet(outSet, currInstruction);
                            return;
                        } else if (defbox.getValue().toString().contains(usebox.getValue().toString())) {
                            if (usebox.getValue().getType() instanceof ArrayType) {
                                addCurrInstInOutSet(outSet, currInstruction);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void addCurrInstInOutSet(FlowSet outSet, Unit currInstruction) {

        UnitContainer currUnitContainer;

        for (ValueBox usebox : currInstruction.getUseBoxes()) {
            if (propertyUseMap.get(usebox.getValue().toString()) == null) {

                List<PropertyAnalysisResult> specialInitInsts = null;
                if (usebox.getValue().toString().matches("r[0-9]+\\.<[^\\>]+>")) {
                    specialInitInsts =
                            FieldInitializationInstructionMap.getInitInstructions(
                                    usebox.getValue().toString().substring(3));
                } else if (usebox.getValue().toString().startsWith("this.")) {
                    specialInitInsts =
                            FieldInitializationInstructionMap.getInitInstructions(
                                    usebox.getValue().toString().substring(5));
                } else if (usebox.getValue().toString().startsWith("<")) {
                    specialInitInsts =
                            FieldInitializationInstructionMap.getInitInstructions(usebox.getValue().toString());
                }

                if (specialInitInsts != null) {
                    propertyUseMap.put(usebox.getValue().toString(), specialInitInsts);
                }
            }
        }

        if (currInstruction instanceof JAssignStmt && currInstruction.toString().contains("invoke ")) {
            currUnitContainer =
                    Utils.createAssignInvokeUnitContainer(currInstruction, initMethod, Utils.DEPTH);
            if (currUnitContainer instanceof AssignInvokeUnitContainer) {
                Set<String> usedProperties =
                        ((AssignInvokeUnitContainer) currUnitContainer).getProperties();
                usedFields.addAll(usedProperties);
            }
        } else if (currInstruction instanceof JInvokeStmt) {
            currUnitContainer =
                    Utils.createInvokeUnitContainer(currInstruction, initMethod, usedFields, Utils.DEPTH);
        } else {
            currUnitContainer = new UnitContainer();
        }

        currUnitContainer.setUnit(currInstruction);
        currUnitContainer.setMethod(initMethod);

        outSet.add(currUnitContainer);
    }

    private void addFakeInstInOutSet(FlowSet outSet, Unit fake, String original, FlowSet inSet) {

        if (original.startsWith("r0.")) {
            original = original.substring(3);
        } else if (original.startsWith("this.")) {
            original = original.substring(5);
        }

        PropertyFakeUnitContainer currUnitContainer = new PropertyFakeUnitContainer();
        currUnitContainer.setUnit(fake);
        currUnitContainer.setMethod(initMethod);
        currUnitContainer.setOriginalProperty(original);

        outSet.add(currUnitContainer);
        outSet.union(inSet);
    }

    private boolean isInvokeOn(Unit currInstruction, ValueBox usebox) {
        return currInstruction instanceof JInvokeStmt
                && currInstruction.toString().contains(usebox.getValue().toString() + ".<");
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

    @Override
    protected Object entryInitialFlow() {
        return emptySet.clone();
    }

    public Map<String, List<PropertyAnalysisResult>> getPropertyUseMap() {
        return propertyUseMap;
    }
}
