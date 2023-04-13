package com.xu.analyzer.backward;

import soot.Unit;

public class UnitContainer {
    private Unit unit;
    private String method;

    /**
     * Getter for the field <code>unit</code>.
     *
     * @return a {@link soot.Unit} object.
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Setter for the field <code>unit</code>.
     *
     * @param unit a {@link soot.Unit} object.
     */
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /**
     * Getter for the field <code>method</code>.
     *
     * @return a {@link String} object.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Setter for the field <code>method</code>.
     *
     * @param method a {@link String} object.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnitContainer that = (UnitContainer) o;

        return unit.toString().equals(that.unit.toString());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return ("UnitContainer{" + "unit=" + unit + ", method='" + method + "\'" + "}").hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "UnitContainer{" + "unit=" + unit + ", method='" + method + '\'' + '}';
    }
}
