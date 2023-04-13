package com.xu.slicer.backward.property;

import com.xu.analyzer.backward.UnitContainer;
import com.xu.analyzer.backward.MethodWrapper;

import java.util.List;
import java.util.Map;

public class PropertyAnalysisResult {
    private MethodWrapper methodWrapper;
    private List<Integer> influencingParams;
    private List<UnitContainer> slicingResult;
    private Map<String, List<PropertyAnalysisResult>> propertyUseMap;

    /**
     * Getter for the field <code>methodWrapper</code>.
     *
     * @return a {@link MethodWrapper} object.
     */
    public MethodWrapper getMethodWrapper() {
        return methodWrapper;
    }

    /**
     * Setter for the field <code>methodWrapper</code>.
     *
     * @param methodWrapper a {@link MethodWrapper} object.
     */
    public void setMethodWrapper(MethodWrapper methodWrapper) {
        this.methodWrapper = methodWrapper;
    }

    /**
     * Getter for the field <code>influencingParams</code>.
     *
     * @return a {@link List} object.
     */
    public List<Integer> getInfluencingParams() {
        return influencingParams;
    }

    /**
     * Setter for the field <code>influencingParams</code>.
     *
     * @param influencingParams a {@link List} object.
     */
    public void setInfluencingParams(List<Integer> influencingParams) {
        this.influencingParams = influencingParams;
    }

    /**
     * Getter for the field <code>slicingResult</code>.
     *
     * @return a {@link List} object.
     */
    public List<UnitContainer> getSlicingResult() {
        return slicingResult;
    }

    /**
     * Setter for the field <code>slicingResult</code>.
     *
     * @param slicingResult a {@link List} object.
     */
    public void setSlicingResult(List<UnitContainer> slicingResult) {
        this.slicingResult = slicingResult;
    }

    /**
     * Getter for the field <code>propertyUseMap</code>.
     *
     * @return a {@link Map} object.
     */
    public Map<String, List<PropertyAnalysisResult>> getPropertyUseMap() {
        return propertyUseMap;
    }

    /**
     * Setter for the field <code>propertyUseMap</code>.
     *
     * @param propertyUseMap a {@link Map} object.
     */
    public void setPropertyUseMap(Map<String, List<PropertyAnalysisResult>> propertyUseMap) {
        this.propertyUseMap = propertyUseMap;
    }
}
