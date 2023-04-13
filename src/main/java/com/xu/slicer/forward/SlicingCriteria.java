package com.xu.slicer.forward;

public class SlicingCriteria {
    private String methodName;

    /**
     * Constructor for SlicingCriteria.
     *
     * @param methodName a {@link String} object.
     */
    public SlicingCriteria(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Getter for the field <code>methodName</code>.
     *
     * @return a {@link String} object.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Setter for the field <code>methodName</code>.
     *
     * @param methodName a {@link String} object.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SlicingCriteria that = (SlicingCriteria) o;

        return methodName.equals(that.methodName);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return methodName.hashCode();
    }
}
