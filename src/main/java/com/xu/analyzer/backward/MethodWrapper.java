package com.xu.analyzer.backward;

import com.xu.slicer.backward.MethodCallSiteInfo;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * MethodWrapper类表示每个方法函数对应的信息
 * isTopLevel ？？布尔值，表示是否为顶层的方法
 * method 用SootMethod格式表示的方法
 * List<> calleeList 本方法调用的方法
 * List<> callerList 调用本方法的方法
 * @author CryptoguardTeam
 * @version 03.07.01
 * @since V01.00.00
 */
public class MethodWrapper {
    private boolean isTopLevel = true;
    private SootMethod method;
    private List<MethodCallSiteInfo> calleeList;
    private List<MethodWrapper> callerList;
    private boolean isInheritedMethod;
    private String subClassName;

    /**
     * Constructor for MethodWrapper.
     *
     * @param method a {@link soot.SootMethod} object.
     */
    public MethodWrapper(SootMethod method) {
        this.method = method;
        this.calleeList = new ArrayList<>();
        this.callerList = new ArrayList<>();
        this.isInheritedMethod = false;
        this.subClassName = "";
    }
    public MethodWrapper(SootMethod method, boolean isInheritedMethod, String subClassName) {
        this.method = method;
        this.calleeList = new ArrayList<>();
        this.callerList = new ArrayList<>();
        this.isInheritedMethod = isInheritedMethod;
        this.subClassName = subClassName;
    }

    /**
     * Getter for the field <code>method</code>.
     *
     * @return a {@link soot.SootMethod} object.
     */
    public SootMethod getMethod() {
        return method;
    }

    /**
     * Setter for the field <code>method</code>.
     *
     * @param method a {@link soot.SootMethod} object.
     */
    public void setMethod(SootMethod method) {
        this.method = method;
    }

    /**
     * Getter for the field <code>calleeList</code>.
     *
     * @return a {@link List} object.
     */
    public List<MethodCallSiteInfo> getCalleeList() {
        return calleeList;
    }

    /**
     * isTopLevel.
     *
     * @return a boolean.
     */
    public boolean isTopLevel() {
        return isTopLevel;
    }

    /**
     * setTopLevel.
     *
     * @param topLevel a boolean.
     */
    public void setTopLevel(boolean topLevel) {
        isTopLevel = topLevel;
    }

    /**
     * Getter for the field <code>callerList</code>.
     *
     * @return a {@link List} object.
     */
    public List<MethodWrapper> getCallerList() {
        return callerList;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodWrapper methodWrapper = (MethodWrapper) o;
        return method.toString().equals(methodWrapper.method.toString());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return method.toString().hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return method.toString();
    }

    public boolean isInheritedMethod() {
        return isInheritedMethod;
    }

    public void setInheritedMethod(boolean inheritedMethod) {
        isInheritedMethod = inheritedMethod;
    }

    public String getSubClassName() {
        return subClassName;
    }

    public void setSubClassName(String subClassName) {
        this.subClassName = subClassName;
    }
}
