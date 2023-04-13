package com.xu.slicer.forward;


import com.xu.slicer.InfluenceInstructions;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForwardInfluenceInstructions implements InfluenceInstructions {
    private SlicingResult slicingResult;
    private List<SlicingResult> slicingResults;
    /**
     * Constructor for ForwardInfluenceInstructions.
     *
     * @param graph a {@link soot.toolkits.graph.DirectedGraph} object.
     * @param slicingCriteria a {@link SlicingCriteria} object.
     */
    public ForwardInfluenceInstructions(DirectedGraph graph, SlicingCriteria slicingCriteria) {
        ForwardProgramSlicing analysis = new ForwardProgramSlicing(graph, slicingCriteria);
        this.slicingResults = new ArrayList<>();

        for (Object aGraph : graph) {
            Unit s = (Unit) aGraph;

            FlowSet set = (FlowSet) analysis.getFlowAfter(s);
            List<Unit> analysisResult = Collections.unmodifiableList(set.toList());

            SlicingResult slicingResult = new SlicingResult();

            slicingResult.setAnalysisResult(analysisResult);
            slicingResult.setCallSiteInfo(analysis.getMethodCallSiteInfo());
            checkSlicingResultExist(slicingResult);
        }
    }

    @Override
    public SlicingResult getSlicingResult() {
        return this.slicingResult;
    }

    private void checkSlicingResultExist(SlicingResult slicingResult){
        if(slicingResult.getAnalysisResult().isEmpty()){
            return ;
        }else {
            if(slicingResults.isEmpty()){
                slicingResults.add(slicingResult);
            }else {
                if(slicingResults.get(slicingResults.size()-1).getAnalysisResult().get(0).toString().contains(slicingResult.getAnalysisResult().get(0).toString())){
                    slicingResults.set(slicingResults.size()-1, slicingResult);
                }else {
                    slicingResults.add(slicingResult);
                }
            }
        }
    }

    public List<SlicingResult> getSlicingResults() {
        return slicingResults;
    }
}
