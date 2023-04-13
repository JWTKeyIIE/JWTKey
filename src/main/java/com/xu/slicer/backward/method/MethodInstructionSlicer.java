package com.xu.slicer.backward.method;

import com.xu.analyzer.backward.UnitContainer;
import com.xu.analyzer.backward.AssignInvokeUnitContainer;
import com.xu.analyzer.backward.InvokeUnitContainer;
import com.xu.analyzer.backward.ParamFakeUnitContainer;
import com.xu.slicer.backward.MethodCallSiteInfo;
import com.xu.slicer.backward.property.PropertyAnalysisResult;
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

public class MethodInstructionSlicer extends BackwardFlowAnalysis {

    private FlowSet emptySet;
    private MethodCallSiteInfo methodCallSiteInfo;
    private List<Integer> slicingParams;
    private List<String> usedFields;
    private Map<String, List<PropertyAnalysisResult>> propertyUseMap;

    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     *
     * @param graph
     */
    public MethodInstructionSlicer(DirectedGraph graph, MethodCallSiteInfo methodCallSiteInfo, List<Integer> slicingParams) {
        super(graph);
        //Sparse Array 稀疏数组就是数组中大部分的内容值都未被使用（或都为零），
        // 在数组中仅有少部分的空间使用。因此造成内存空间的浪费，为了节省内存空间.
        this.emptySet = new ValueArraySparseSet();
        this.methodCallSiteInfo = methodCallSiteInfo;
        this.slicingParams = slicingParams;
        this.usedFields = new ArrayList<>();
        this.propertyUseMap = new HashMap<>();
        doAnalysis();
    }


    /** {@inheritDoc}
     * flowThrough数据流的传递函数问题，flowThrough函数的参数有三个
     * in-set：输入的参数
     * node： 被处理的节点（被处理的语句）
     * out-set：输出的参数*/
    @Override
    protected void flowThrough(Object in, Object node, Object out) {
        FlowSet inSet = (FlowSet) in, outSet = (FlowSet) out;
        Unit currInstruction = (Unit) node;

        //如果要处理的语句是正在处理的调用切片标准的方法中的语句时
        String compareCriteriaInstruction = methodCallSiteInfo.getCallee().toString();
        if(methodCallSiteInfo.getCallee().isInheritedMethod()){
            compareCriteriaInstruction = "<" + methodCallSiteInfo.getCallee().getSubClassName() + ":" + methodCallSiteInfo.getCallee().getMethod().getSignature().split(":")[1];
        }
/*        if(currInstruction.toString().contains(methodCallSiteInfo.getCallee().toString())
        && currInstruction.getJavaSourceStartLineNumber() == methodCallSiteInfo.getLineNumber()){*/
        if(currInstruction.toString().contains(compareCriteriaInstruction)
                && currInstruction.getJavaSourceStartLineNumber() == methodCallSiteInfo.getLineNumber()){

            //针对切片参数进行处理
            for(Integer slicingParam : slicingParams) {

                Value valueToAssign = null;
           /*     Value valueToAssign2 = null;*/

                //getUseBoxes：检索被Unit使用的值， getDefBoxes：检索被Unit定义的值
                for(ValueBox usebox : currInstruction.getUseBoxes()) {

                    //找到切片参数被定义的变量
                    if(usebox.getValue().toString().contains("invoke ")
                    && usebox.getValue().getUseBoxes().size() > slicingParam) {
                        valueToAssign = usebox.getValue().getUseBoxes().get(slicingParam).getValue();
                        /*if(usebox.getValue().toString().contains("specialinvoke ") && usebox.getValue().getUseBoxes().size() > slicingParam){
                            valueToAssign2 = usebox.getValue().getUseBoxes().get(usebox.getValue().getUseBoxes().size()-1).getValue();
                        }*/
                        break;
                    }

                }

                if(valueToAssign != null) {

                    Value localValue =
                            new BafLocal(
                                    "$fakeLocal_" + methodCallSiteInfo.getLineNumber() + slicingParams,
                                    valueToAssign.getType());
                    AssignStmt assignStmt = Jimple.v().newAssignStmt(localValue, valueToAssign);

                    ParamFakeUnitContainer container = new ParamFakeUnitContainer();

                    container.setUnit(assignStmt);
                    container.setParam(slicingParam);
                    container.setCallee(methodCallSiteInfo.getCallee().toString());
                    container.setMethod(methodCallSiteInfo.getCaller().toString());

                    outSet.add(container);
                }
/*                if(valueToAssign2 != null) {
                    Value localValue =
                            new BafLocal(
                                    "$fakeLocal_" + methodCallSiteInfo.getLineNumber() + slicingParams,
                                    valueToAssign2.getType());
                    AssignStmt assignStmt = Jimple.v().newAssignStmt(localValue, valueToAssign2);

                    ParamFakeUnitContainer container = new ParamFakeUnitContainer();

                    container.setUnit(assignStmt);
                    container.setParam(slicingParam);
                    container.setCallee(methodCallSiteInfo.getCallee().toString());
                    container.setMethod(methodCallSiteInfo.getCaller().toString());

                    outSet.add(container);
                }*/
            }
            return;
        }

        //当要处理的语句不是切片标准语句的时候
        //如果输入参数不是空
        if (!inSet.isEmpty()) {

            outSet.union(inSet);

            //如果是if语句，则直接返回
            if (currInstruction.toString().startsWith("if ")) {
                return;
            }
            //否则遍历所有输入inset
            for (Object anInSet : inSet.toList()) {

                UnitContainer insetInstruction = (UnitContainer) anInSet;
                List<ValueBox> useBoxes = insetInstruction.getUnit().getUseBoxes();

                //遍历In set语句中的所有Useboxes（用于分析Out set）
                for (ValueBox usebox : useBoxes) {
                    //如果是实例化对象相关的语句
                    if ((usebox.getValue().toString().equals("r0")
                            && insetInstruction.getUnit().toString().contains("r0."))
                            || (usebox.getValue().toString().equals("this")
                            && insetInstruction.getUnit().toString().contains("this."))) {
                        continue;
                    }

                    /**
                     * 如果当前的in set语句是赋值调用语句。
                     * 例如：byte[] keyBytes = DatatypeConverter.parseBase64Binary(key);
                     * key 为 r7
                     * UnitContainer
                     * {unit=
                     * r0 = staticinvoke <javax.xml.bind.DatatypeConverter:
                     * byte[] parseBase64Binary(java.lang.String)>(r7),
                     * method='<tester.SymCrypto: byte[] encrypt(java.lang.String,java.lang.String)>'}
                     */
                    if (insetInstruction instanceof AssignInvokeUnitContainer) {

                        //判断in set语句中的参数是不是复制调用语句的参数
                        int arg = Utils.isArgOfAssignInvoke(usebox, insetInstruction.getUnit());

                        if (arg > -1) {
                            String args = ((AssignInvokeUnitContainer) insetInstruction).getArgs().toString();
                            if (!args.contains("" + arg)) {
                                continue;
                            }
                        }
                    }

                    if (insetInstruction instanceof InvokeUnitContainer) {
                        //判断in set语句（当前use box）中的参数是不是调用中的参数
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

                    /**
                     * 检查当前处理的Current Instruction
                     * 如果是定义语句，则需要在Out set语句中添加新的节点
                     */
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

    /**
     * 将当前指令加入到 out Set中
     * @param outSet
     * @param currInstruction
     */
    private void addCurrInstInOutSet(FlowSet outSet, Unit currInstruction) {

        UnitContainer currUnitContainer;

        for(ValueBox useBox : currInstruction.getUseBoxes()) {
            if (propertyUseMap.get(useBox.getValue().toString()) == null) {
                List<PropertyAnalysisResult> specialInitInsts = null;

                /**
                 * 例如指令 $r6 = r3.<tester.SymCrypto: java.lang.String defaultKey>
                 */
                if(useBox.getValue().toString().matches("r[0-9]+\\.<[^\\>]+>")) {
                    specialInitInsts = FieldInitializationInstructionMap.getInitInstructions(
                            useBox.getValue().toString().substring(3));
                }else if(useBox.getValue().toString().startsWith("this.")) {
                    specialInitInsts =
                            FieldInitializationInstructionMap.getInitInstructions(
                                    useBox.getValue().toString().substring(5));
                }
                /**
                 * 例如指令 $r0 = <tester.NewTestCase2: char[] encryptKey>
                 */
                else if(useBox.getValue().toString().startsWith("<")) {
                    specialInitInsts =
                            FieldInitializationInstructionMap.getInitInstructions(useBox.getValue().toString());
                }

                if (specialInitInsts != null) {
                    propertyUseMap.put(useBox.getValue().toString(), specialInitInsts);
                }
            }
        }
        //如果当前指令是赋值调用语句，需要分析被调用的函数进行切片
        if (currInstruction instanceof JAssignStmt && currInstruction.toString().contains("invoke ")) {
            currUnitContainer =
                    Utils.createAssignInvokeUnitContainer(
                            currInstruction, methodCallSiteInfo.getCaller().toString(), Utils.DEPTH);

            if(currInstruction instanceof AssignInvokeUnitContainer){
                Set<String> usedProperties =
                        ((AssignInvokeUnitContainer)currUnitContainer).getProperties();
                usedFields.addAll(usedProperties);
            }
        }
        //如果当前语句是调用语句，也需要继续对调用的方法进行切片分析
        else if (currInstruction instanceof JInvokeStmt) {
            currUnitContainer =
                    Utils.createInvokeUnitContainer(currInstruction, methodCallSiteInfo.getCaller().toString(), usedFields, Utils.DEPTH);
        }
        //如果既不是赋值调用也不是调用语句，则直接创建新的UnitContainer，将现在正在分析的语句加入
        else {
            currUnitContainer = new UnitContainer();
        }

        currUnitContainer.setUnit(currInstruction);
        currUnitContainer.setMethod(methodCallSiteInfo.getCaller().toString());

        outSet.add(currUnitContainer);

    }

    /**
     * 如果当前指令是对use box中的参数的调用，返回真
     * @param currInstruction
     * @param useBox
     * @return
     */
    private boolean isInvokeOn(Unit currInstruction, ValueBox useBox) {
        return currInstruction instanceof JInvokeStmt
                && currInstruction.toString().contains(useBox.getValue().toString() + ".<");
    }

    @Override
    protected Object newInitialFlow() {
        return emptySet.clone();
    }

    @Override
    protected Object entryInitialFlow() {
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

    public MethodCallSiteInfo getMethodCallSiteInfo() {
        return methodCallSiteInfo;
    }

    public Map<String, List<PropertyAnalysisResult>> getPropertyUseMap() {
        return propertyUseMap;
    }
}
