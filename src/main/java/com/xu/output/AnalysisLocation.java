package com.xu.output;

public class AnalysisLocation {

    private Integer lineStart = null;
    private Integer lineEnd = null;
    private Integer colStart = null;
    private Integer colEnd = null;
    private Integer methodNumber = -1;

    public AnalysisLocation(Integer start, Integer end) {
        this.lineStart = start;
        this.lineEnd = end;
    }

    /**
     * Constructor for AnalysisLocation.
     *
     * @param lineNumber a {@link Integer} object.
     */
    public AnalysisLocation(Integer lineNumber) {
        this.lineStart = lineNumber;
        this.lineEnd = lineNumber;
    }

    /**
     * Constructor for AnalysisLocation.
     *
     * @param start a {@link Integer} object.
     * @param end a {@link Integer} object.
     * @param methodNumber a {@link Integer} object.
     */
    public AnalysisLocation(Integer start, Integer end, Integer methodNumber) {
        this.lineStart = start;
        this.lineEnd = end;
        this.methodNumber = methodNumber;
    }
    //endregion

    //region Overridden Methods

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();

        output.append(this.lineStart);

        if (!this.lineEnd.equals(this.lineStart)) {
            output.append("-");
            output.append(this.lineEnd);
        }

        return output.toString();
    }
    //endregion

    //region Getters

    /**
     * Getter for the field <code>lineStart</code>.
     *
     * @return a {@link Integer} object.
     */
    public Integer getLineStart() {
        return lineStart;
    }

    /**
     * Getter for the field <code>lineEnd</code>.
     *
     * @return a {@link Integer} object.
     */
    public Integer getLineEnd() {
        return lineEnd;
    }

    /**
     * Getter for the field <code>methodNumber</code>.
     *
     * @return a {@link Integer} object.
     */
    public Integer getMethodNumber() {
        return methodNumber;
    }

    /**
     * Setter for the field <code>methodNumber</code>.
     *
     * @param methodNumber a {@link Integer} object.
     */
    public void setMethodNumber(Integer methodNumber) {
        this.methodNumber = methodNumber;
    }

    /**
     * Getter for colStart
     *
     * <p>getColStart()
     *
     * @return {@link Integer} - The colStart.
     */
    public Integer getColStart() {
        return colStart;
    }

    /**
     * Setter for colStart
     *
     * <p>setColStart(java.lang.Integer colStart)
     *
     * @param colStart {@link Integer} - The value to set as colStart
     */
    public void setColStart(Integer colStart) {
        this.colStart = colStart;
    }

    /**
     * Getter for colEnd
     *
     * <p>getColEnd()
     *
     * @return {@link Integer} - The colEnd.
     */
    public Integer getColEnd() {
        return colEnd;
    }

    /**
     * Setter for colEnd
     *
     * <p>setColEnd(java.lang.Integer colEnd)
     *
     * @param colEnd {@link Integer} - The value to set as colEnd
     */
    public void setColEnd(Integer colEnd) {
        this.colEnd = colEnd;
    }
    //endregion
}
